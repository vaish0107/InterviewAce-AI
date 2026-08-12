import json

import httpx
from google import genai
from google.genai import errors, types
from pydantic import ValidationError

from app.core.config import Settings, get_settings
from app.models.interview import (
    AnswerEvaluationRequest,
    AnswerEvaluationResponse,
)

EVALUATION_NOTE = (
    "This feedback is AI-generated and should be treated as guidance "
    "rather than an objective hiring decision."
)

SYSTEM_INSTRUCTION = """
You evaluate only the supplied interview question and candidate answer.

Do not infer identity, protected attributes, unmentioned experience,
or personality. Do not fabricate technical claims.

Do not reward verbosity alone. Concise correct answers may score highly,
and confident incorrect answers must not.

For TECHNICAL questions:
- assess correctness
- important concepts
- relevance
- clarity
- completeness

For HR questions:
- do not assume one objectively correct answer
- assess relevance
- structure
- clarity
- specificity
- professionalism
- completeness

For PROJECT questions:
- assess problem/solution clarity
- stated contribution
- technical reasoning
- concrete outcomes only when mentioned

The improved answer must remain realistic and must not introduce
experience or facts the candidate did not provide.

Score using exactly these maximum values:

relevance_score: 0-25
correctness_score: 0-35
completeness_score: 0-25
communication_score: 0-15

overall_score must equal the exact sum of those four dimensions.

Return only the requested structured output.
"""


class EvaluationConfigurationError(RuntimeError):
    pass


class EvaluationUnavailableError(RuntimeError):
    pass


class EvaluationOutputError(RuntimeError):
    pass


class AnswerEvaluator:
    def __init__(
        self,
        settings: Settings | None = None,
        client: genai.Client | None = None,
    ):
        self.settings = settings or get_settings()
        self.client = client

    def evaluate(
        self,
        request: AnswerEvaluationRequest,
    ) -> AnswerEvaluationResponse:

        if not self.settings.gemini_api_key.strip() and self.client is None:
            raise EvaluationConfigurationError(
                "Answer evaluation is not configured"
            )

        client = self.client or genai.Client(
            api_key=self.settings.gemini_api_key,
            http_options=types.HttpOptions(
                timeout=30_000,
                retry_options=types.HttpRetryOptions(
                    attempts=2
                ),
            ),
        )

        payload = {
            "question": request.question,
            "answer": request.answer,
            "category": request.category,
            "skill": request.skill,
            "difficulty": request.difficulty,
        }

        try:
            response = client.models.generate_content(
                model=self.settings.gemini_model,
                contents=json.dumps(
                    payload,
                    ensure_ascii=False,
                ),
                config=types.GenerateContentConfig(
                    system_instruction=SYSTEM_INSTRUCTION,
                    response_mime_type="application/json",
                    response_schema=AnswerEvaluationResponse,
                    temperature=0.2,
                ),
            )

            if response.parsed is None:
                raise EvaluationOutputError(
                    "AI answer evaluation returned an invalid response"
                )

            result = AnswerEvaluationResponse.model_validate(
                response.parsed
            )

        except EvaluationOutputError:
            raise

        except errors.APIError as error:
            # Server-side debugging only.
            # This does NOT expose the Gemini API key.
            print(
                f"Gemini API error: "
                f"code={error.code}, "
                f"message={error.message}"
            )

            raise EvaluationUnavailableError(
                "AI answer evaluation is temporarily unavailable"
            ) from error

        except (
            httpx.TimeoutException,
            httpx.NetworkError,
            TimeoutError,
            ConnectionError,
        ) as error:
            print(
                "Gemini network/timeout error: "
                f"{type(error).__name__}: {error}"
            )

            raise EvaluationUnavailableError(
                "AI answer evaluation is temporarily unavailable"
            ) from error

        except (
            ValidationError,
            ValueError,
            TypeError,
            json.JSONDecodeError,
        ) as error:
            print(
                "Gemini output validation error: "
                f"{type(error).__name__}: {error}"
            )

            raise EvaluationOutputError(
                "AI answer evaluation returned an invalid response"
            ) from error

        expected_score = (
            result.relevance_score
            + result.correctness_score
            + result.completeness_score
            + result.communication_score
        )

        if result.overall_score != expected_score:
            raise EvaluationOutputError(
                "AI answer evaluation returned inconsistent scores"
            )

        return result.model_copy(
            update={
                "evaluation_note": EVALUATION_NOTE
            }
        )