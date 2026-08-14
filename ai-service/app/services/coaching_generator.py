import json
from collections import Counter, defaultdict

import httpx
from google import genai
from google.genai import errors, types
from pydantic import ValidationError

from app.core.config import Settings, get_settings
from app.models.interview import InterviewCoachingRequest, InterviewCoachingResponse

COACHING_NOTE = "This coaching plan is AI-generated from your saved interview responses and evaluations. It is intended for practice guidance, not hiring assessment."

SYSTEM_INSTRUCTION = """
Create a concise interview practice coaching plan using only the supplied saved answers,
evaluations, and deterministic signals. Prioritize repeated measurable patterns. Do not
predict hiring outcomes, rank people, infer intelligence/personality, invent experience,
or add unsupported claims. An example improved approach is guidance, not the sole correct
answer. Avoid generic motivational filler. Return only the requested structured output.
"""

class CoachingConfigurationError(RuntimeError): pass
class CoachingUnavailableError(RuntimeError): pass
class CoachingOutputError(RuntimeError): pass


def deterministic_signals(request: InterviewCoachingRequest) -> dict:
    evaluated = [a for a in request.answers if a.overall_score is not None]
    dimensions = {name: [getattr(a, name) for a in evaluated if getattr(a, name) is not None]
                  for name in ("relevance_score", "correctness_score", "completeness_score", "communication_score")}
    by_skill: dict[str, list[int]] = defaultdict(list)
    by_category: dict[str, list[int]] = defaultdict(list)
    for item in evaluated:
        if item.skill: by_skill[item.skill].append(item.overall_score)
        by_category[item.category].append(item.overall_score)
    missing = Counter(point.strip() for item in evaluated for point in item.missing_key_points if point.strip())
    weaknesses = Counter(point.strip() for item in evaluated for point in item.weaknesses if point.strip())
    average = lambda values: round(sum(values) / len(values), 2) if values else None
    return {
        "evaluated_answer_count": len(evaluated),
        "average_scores": {key.removesuffix("_score"): average(values) for key, values in dimensions.items()},
        "lowest_scoring_skills": sorted(({"skill": k, "average": average(v)} for k, v in by_skill.items()), key=lambda x: x["average"])[:3],
        "lowest_category": min(({"category": k, "average": average(v)} for k, v in by_category.items()), key=lambda x: x["average"], default=None),
        "repeated_missing_concepts": [k for k, count in missing.most_common() if count >= 2],
        "repeated_weaknesses": [k for k, count in weaknesses.most_common() if count >= 2],
    }


class CoachingGenerator:
    def __init__(self, settings: Settings | None = None, client: genai.Client | None = None):
        self.settings = settings or get_settings(); self.client = client

    def generate(self, request: InterviewCoachingRequest) -> InterviewCoachingResponse:
        signals = deterministic_signals(request)
        if not signals["evaluated_answer_count"]:
            raise CoachingOutputError("Evaluate at least one interview answer before generating a coaching plan.")
        if not self.settings.gemini_api_key.strip() and self.client is None:
            raise CoachingConfigurationError("Interview coaching is not configured")
        client = self.client or genai.Client(api_key=self.settings.gemini_api_key, http_options=types.HttpOptions(timeout=30_000, retry_options=types.HttpRetryOptions(attempts=2)))
        payload = {"interview_type": request.interview_type, "difficulty": request.difficulty,
                   "answers": [a.model_dump() for a in request.answers], "deterministic_signals": signals}
        try:
            response = client.models.generate_content(model=self.settings.gemini_model,
                contents=json.dumps(payload, ensure_ascii=False), config=types.GenerateContentConfig(
                    system_instruction=SYSTEM_INSTRUCTION, response_mime_type="application/json",
                    response_schema=InterviewCoachingResponse, temperature=0.2))
            if response.parsed is None: raise CoachingOutputError("AI coaching returned an invalid response")
            result = InterviewCoachingResponse.model_validate(response.parsed)
            return result.model_copy(update={"coaching_note": COACHING_NOTE})
        except CoachingOutputError: raise
        except errors.APIError as error:
            raise CoachingUnavailableError("AI coaching is temporarily unavailable") from error
        except (httpx.TimeoutException, httpx.NetworkError, TimeoutError, ConnectionError) as error:
            raise CoachingUnavailableError("AI coaching is temporarily unavailable") from error
        except (ValidationError, ValueError, TypeError, json.JSONDecodeError) as error:
            raise CoachingOutputError("AI coaching returned an invalid response") from error
