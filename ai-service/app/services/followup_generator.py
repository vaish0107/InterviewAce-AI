import json

import httpx
from google import genai
from google.genai import errors, types
from pydantic import ValidationError

from app.core.config import Settings, get_settings
from app.models.interview import FollowUpQuestionRequest, FollowUpQuestionResponse

MAX_FOLLOWUPS_PER_BASE_QUESTION = 2
MIN_MEANINGFUL_ANSWER_LENGTH = 12

SYSTEM_INSTRUCTION = """
Decide whether one adaptive interview follow-up would add genuine value.
Ground it only in the supplied question, candidate answer, skill/category, and limited context.
Never invent candidate experience or introduce unmentioned project technologies.
Do not infer protected attributes or personality and do not judge hiring suitability.
Ask at most one concise question, avoid previous follow-up duplicates, and follow the difficulty.
Return should_ask_followup=false when the answer is empty, too short, says the candidate does not
know, is already sufficiently complete, or further drilling would not be useful.
Return only the requested structured output.
"""


class FollowUpConfigurationError(RuntimeError): pass
class FollowUpUnavailableError(RuntimeError): pass
class FollowUpOutputError(RuntimeError): pass


class FollowUpGenerator:
    def __init__(self, settings: Settings | None = None, client: genai.Client | None = None):
        self.settings = settings or get_settings()
        self.client = client

    def generate(self, request: FollowUpQuestionRequest) -> FollowUpQuestionResponse:
        answer = request.candidate_answer.strip()
        if len(request.previous_followups) >= MAX_FOLLOWUPS_PER_BASE_QUESTION:
            return FollowUpQuestionResponse(should_ask_followup=False, reason="Maximum follow-up limit reached.")
        if len(answer) < MIN_MEANINGFUL_ANSWER_LENGTH:
            return FollowUpQuestionResponse(should_ask_followup=False, reason="The answer is too short to generate a useful follow-up.")
        if not self.settings.gemini_api_key.strip() and self.client is None:
            raise FollowUpConfigurationError("Adaptive follow-up generation is not configured")
        client = self.client or genai.Client(api_key=self.settings.gemini_api_key, http_options=types.HttpOptions(timeout=30_000, retry_options=types.HttpRetryOptions(attempts=2)))
        payload = request.model_dump()
        payload["recent_context"] = payload["recent_context"][-3:]
        try:
            response = client.models.generate_content(
                model=self.settings.gemini_model,
                contents=json.dumps(payload, ensure_ascii=False),
                config=types.GenerateContentConfig(system_instruction=SYSTEM_INSTRUCTION, response_mime_type="application/json", response_schema=FollowUpQuestionResponse, temperature=0.2),
            )
            if response.parsed is None: raise FollowUpOutputError("AI follow-up generation returned an invalid response")
            result = FollowUpQuestionResponse.model_validate(response.parsed)
            if result.should_ask_followup and (not result.question or not result.question.strip()):
                raise FollowUpOutputError("AI follow-up generation returned an invalid question")
            if result.question and any(result.question.strip().casefold() == item.strip().casefold() for item in request.previous_followups):
                return FollowUpQuestionResponse(should_ask_followup=False, reason="The suggested follow-up duplicated an earlier question.")
            return result
        except FollowUpOutputError: raise
        except errors.APIError as error:
            raise FollowUpUnavailableError("Adaptive follow-up generation is temporarily unavailable") from error
        except (httpx.TimeoutException, httpx.NetworkError, TimeoutError, ConnectionError) as error:
            raise FollowUpUnavailableError("Adaptive follow-up generation is temporarily unavailable") from error
        except (ValidationError, ValueError, TypeError, json.JSONDecodeError) as error:
            raise FollowUpOutputError("AI follow-up generation returned an invalid response") from error
