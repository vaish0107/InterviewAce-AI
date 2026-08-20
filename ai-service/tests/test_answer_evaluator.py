import httpx
import pytest
from fastapi.testclient import TestClient
from google.genai import errors, types

import app.api.interview as interview_api
from app.core.config import Settings
from app.main import app
from app.models.interview import AnswerEvaluationRequest, AnswerEvaluationResponse
from app.services.answer_evaluator import (
    EVALUATION_NOTE,
    AnswerEvaluator,
    EvaluationConfigurationError,
    EvaluationOutputError,
    EvaluationUnavailableError,
)

client = TestClient(app)


def evaluation(**changes) -> AnswerEvaluationResponse:
    values = {
        "overall_score": 82,
        "relevance_score": 22,
        "correctness_score": 28,
        "completeness_score": 20,
        "communication_score": 12,
        "strengths": ["Clear and relevant"],
        "weaknesses": ["Could explain lifecycle details"],
        "missing_key_points": ["Mention inversion of control"],
        "improved_answer": "Dependency injection supplies a class with its required collaborators.",
        "summary": "A strong concise answer with room for one more key concept.",
        "evaluation_note": "model supplied note",
    }
    values.update(changes)
    return AnswerEvaluationResponse(**values)


class FakeModels:
    def __init__(self, output=None, error: Exception | None = None): self.output = output; self.error = error; self.calls = []
    def generate_content(self, **kwargs):
        self.calls.append(kwargs)
        if self.error: raise self.error
        return type("FakeResponse", (), {"parsed": self.output})()


class FakeClient:
    def __init__(self, output=None, error: Exception | None = None): self.models = FakeModels(output, error)


def evaluator(output=None, error=None) -> tuple[AnswerEvaluator, FakeClient]:
    fake = FakeClient(output or evaluation(), error)
    return AnswerEvaluator(Settings(gemini_api_key="test-key", gemini_model="test-model"), fake), fake


@pytest.mark.parametrize("category", ["TECHNICAL", "HR", "PROJECT"])
def test_valid_category_evaluation_uses_structured_response(category: str) -> None:
    service, fake = evaluator()
    result = service.evaluate(AnswerEvaluationRequest(question="Explain your approach.", answer="I explained the relevant approach clearly.", category=category, skill=None, difficulty="MEDIUM"))
    assert result.overall_score == 82
    assert result.evaluation_note == EVALUATION_NOTE
    call = fake.models.calls[0]
    assert call["config"].response_schema is AnswerEvaluationResponse
    assert call["config"].response_mime_type == "application/json"
    assert f'"category": "{category}"' in call["contents"]


def test_blank_or_short_answer_is_rejected() -> None:
    response = client.post("/api/interview/evaluate-answer", json={"question": "Question", "answer": "  ", "category": "HR", "skill": None, "difficulty": "EASY"})
    assert response.status_code == 422


def test_dimension_score_bounds_are_validated() -> None:
    with pytest.raises(ValueError):
        evaluation(relevance_score=26)


def test_total_must_equal_dimension_sum() -> None:
    service, _ = evaluator(evaluation(overall_score=90))
    with pytest.raises(EvaluationOutputError, match="inconsistent scores"):
        service.evaluate(AnswerEvaluationRequest(question="Question?", answer="A sufficiently long answer.", category="TECHNICAL", skill="Java", difficulty="EASY"))


def test_malformed_structured_output_is_rejected() -> None:
    service, _ = evaluator(output={"not": "a model"})
    with pytest.raises(EvaluationOutputError):
        service.evaluate(AnswerEvaluationRequest(question="Question?", answer="A sufficiently long answer.", category="PROJECT", skill=None, difficulty="HARD"))


def test_gemini_timeout_is_returned_as_safe_unavailable_error() -> None:
    timeout = httpx.ReadTimeout("timed out", request=httpx.Request("POST", "https://generativelanguage.googleapis.com"))
    service, _ = evaluator(error=timeout)
    with pytest.raises(EvaluationUnavailableError, match="temporarily unavailable"):
        service.evaluate(AnswerEvaluationRequest(question="Question?", answer="A sufficiently long answer.", category="HR", skill=None, difficulty="MEDIUM"))


def test_missing_api_key_is_rejected_without_network_call(caplog) -> None:
    service = AnswerEvaluator(Settings(gemini_api_key="", gemini_model="test-model"))
    with caplog.at_level("ERROR"), pytest.raises(EvaluationConfigurationError):
        service.evaluate(AnswerEvaluationRequest(question="Question?", answer="A sufficiently long answer.", category="HR", skill=None, difficulty="MEDIUM"))
    assert "GEMINI_API_KEY is missing" in caplog.text
    assert "test-key" not in caplog.text


def test_endpoint_maps_unavailable_service_to_safe_error(monkeypatch) -> None:
    class Unavailable:
        def evaluate(self, request): raise EvaluationUnavailableError("AI answer evaluation is temporarily unavailable")
    monkeypatch.setattr(interview_api, "evaluator", Unavailable())
    response = client.post("/api/interview/evaluate-answer", json={"question": "Tell me about yourself.", "answer": "I am a backend developer focused on reliable APIs.", "category": "HR", "skill": None, "difficulty": "EASY"})
    assert response.status_code == 503
    assert response.json()["detail"] == "AI answer evaluation is temporarily unavailable"


@pytest.mark.parametrize("status_code", [400, 401, 404, 429, 503])
def test_gemini_provider_errors_are_returned_as_safe_unavailable_errors(status_code: int) -> None:
    provider_error = errors.ClientError(status_code, {"error": {"message": "provider detail"}}) if status_code < 500 else errors.ServerError(status_code, {"error": {"message": "provider detail"}})
    service, _ = evaluator(error=provider_error)
    with pytest.raises(EvaluationUnavailableError, match="temporarily unavailable"):
        service.evaluate(AnswerEvaluationRequest(question="Question?", answer="A sufficiently long answer.", category="TECHNICAL", skill="Java", difficulty="MEDIUM"))


def test_provider_error_logging_has_status_but_not_provider_detail(caplog) -> None:
    provider_error = errors.ClientError(429, {"error": {"message": "sensitive provider detail"}})
    service, _ = evaluator(error=provider_error)
    with caplog.at_level("ERROR"), pytest.raises(EvaluationUnavailableError):
        service.evaluate(AnswerEvaluationRequest(question="Question?", answer="A sufficiently long answer.", category="TECHNICAL", skill="Java", difficulty="MEDIUM"))
    assert "status_code=429" in caplog.text
    assert "sensitive provider detail" not in caplog.text


def test_endpoint_response_schema_is_preserved(monkeypatch) -> None:
    class Successful:
        def evaluate(self, request): return evaluation()
    monkeypatch.setattr(interview_api, "evaluator", Successful())
    response = client.post("/api/interview/evaluate-answer", json={"question": "Explain DI.", "answer": "Dependencies are supplied to a class from outside.", "category": "TECHNICAL", "skill": "Java", "difficulty": "EASY"})
    assert response.status_code == 200
    assert set(response.json()) == set(AnswerEvaluationResponse.model_fields)

@pytest.mark.parametrize("category", ["TECHNICAL", "HR"])
def test_normal_and_targeted_contract_reach_mocked_evaluator(monkeypatch, category: str) -> None:
    captured = []
    class Successful:
        def evaluate(self, request): captured.append(request); return evaluation()
    monkeypatch.setattr(interview_api, "evaluator", Successful())
    response = client.post("/api/interview/evaluate-answer", json={"question":"Explain the selected practice concept.","answer":"This is a sufficiently detailed saved answer.","category":category,"skill":"Java" if category == "TECHNICAL" else None,"difficulty":"MEDIUM"})
    assert response.status_code == 200
    assert captured[0].category == category
    assert captured[0].difficulty == "MEDIUM"

def test_422_reports_safe_field_and_reason(caplog) -> None:
    with caplog.at_level("WARNING"):
        response = client.post("/api/interview/evaluate-answer", json={"question":"Question","answer":"short","category":"TARGETED_PRACTICE","skill":None,"difficulty":"Medium"})
    assert response.status_code == 422
    assert "body.answer" in caplog.text
    assert "body.category" in caplog.text
