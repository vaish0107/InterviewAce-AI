import json

import httpx
from google import genai
from google.genai import errors, types
from pydantic import ValidationError

from app.core.config import Settings, get_settings
from app.models.interview import TargetedPracticeRequest, TargetedPracticeResponse

SYSTEM_INSTRUCTION = """Generate concise interview-practice questions using only the supplied focus area, skill, difficulty, and weakness context. Directly target understanding rather than trivia. Do not invent candidate experience, infer personality or intelligence, predict hiring success, or produce discriminatory content. Avoid supplied questions and duplicates. Return exactly the requested question_count. Every item must contain question, category, skill, difficulty, and focus_concept using those exact snake_case field names. category and difficulty must be uppercase. For five questions, prefer a progression from fundamentals through deeper reasoning when appropriate. Return only structured output."""


class TargetedPracticeConfigurationError(RuntimeError): pass
class TargetedPracticeUnavailableError(RuntimeError): pass
class TargetedPracticeOutputError(RuntimeError): pass


class TargetedPracticeGenerator:
    def __init__(self, settings: Settings | None = None, client: genai.Client | None = None):
        self.settings = settings or get_settings()
        self.client = client

    @staticmethod
    def _validate_response(response: object, request: TargetedPracticeRequest) -> TargetedPracticeResponse:
        parsed = getattr(response, "parsed", None)
        if parsed is None:
            response_text = getattr(response, "text", None)
            print("Gemini targeted-practice parsed response is None: "
                  f"text_present={bool(response_text)}, "
                  f"text_length={len(response_text) if isinstance(response_text, str) else 0}")
            raise TargetedPracticeOutputError("AI targeted practice returned an invalid response")
        try:
            result = TargetedPracticeResponse.model_validate(parsed)
        except ValidationError as error:
            print("Targeted practice validation error:", type(error).__name__, str(error))
            raise TargetedPracticeOutputError("AI targeted practice returned an invalid response") from error
        if len(result.questions) != request.question_count:
            print(f"Targeted practice question-count mismatch: expected={request.question_count}, actual={len(result.questions)}")
            raise TargetedPracticeOutputError("AI targeted practice returned the wrong question count")
        normalized = [" ".join(item.question.split()).casefold() for item in result.questions]
        avoided = {" ".join(item.split()).casefold() for item in request.avoid_questions}
        if len(set(normalized)) != len(normalized) or any(item in avoided for item in normalized):
            print("Targeted practice duplicate-question validation failed")
            raise TargetedPracticeOutputError("AI targeted practice returned duplicate questions")
        if any(item.difficulty != request.difficulty for item in result.questions):
            print("Targeted practice difficulty validation failed")
            raise TargetedPracticeOutputError("AI targeted practice returned the wrong difficulty")
        return result

    def generate(self, request: TargetedPracticeRequest) -> TargetedPracticeResponse:
        if not self.settings.gemini_api_key.strip() and self.client is None:
            raise TargetedPracticeConfigurationError("Targeted practice generation is not configured")
        client = self.client or genai.Client(api_key=self.settings.gemini_api_key,
            http_options=types.HttpOptions(timeout=30_000, retry_options=types.HttpRetryOptions(attempts=2)))
        config = types.GenerateContentConfig(system_instruction=SYSTEM_INSTRUCTION,
            response_mime_type="application/json", response_schema=TargetedPracticeResponse, temperature=0.25)
        try:
            last_error = None
            for attempt in range(2):
                response = client.models.generate_content(model=self.settings.gemini_model,
                    contents=json.dumps(request.model_dump(), ensure_ascii=False), config=config)
                try:
                    return self._validate_response(response, request)
                except TargetedPracticeOutputError as error:
                    last_error = error
                    if attempt == 0:
                        print("Retrying targeted practice after invalid structured response")
            raise last_error
        except TargetedPracticeOutputError:
            raise
        except errors.APIError as error:
            code = getattr(error, "code", None)
            message = getattr(error, "message", str(error))
            print(f"Gemini targeted-practice API error: code={code}, message={message}")
            if code == 429 or (isinstance(code, int) and code >= 500):
                raise TargetedPracticeUnavailableError("AI targeted practice is temporarily unavailable") from error
            raise TargetedPracticeOutputError("AI targeted practice request was rejected") from error
        except (httpx.TimeoutException, httpx.NetworkError, TimeoutError, ConnectionError) as error:
            print("Gemini targeted-practice transport error:", type(error).__name__, str(error))
            raise TargetedPracticeUnavailableError("AI targeted practice is temporarily unavailable") from error
        except (ValueError, TypeError, json.JSONDecodeError) as error:
            print("Targeted practice malformed response:", type(error).__name__, str(error))
            raise TargetedPracticeOutputError("AI targeted practice returned an invalid response") from error
