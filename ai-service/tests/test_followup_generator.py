import httpx
import pytest

import app.api.interview as interview_api
from app.core.config import Settings
from app.main import app
from app.models.interview import FollowUpQuestionRequest, FollowUpQuestionResponse
from app.services.followup_generator import FollowUpGenerator, FollowUpOutputError, FollowUpUnavailableError
from fastapi.testclient import TestClient


class FakeModels:
    def __init__(self, output=None, error=None): self.output = output; self.error = error; self.calls = []
    def generate_content(self, **kwargs):
        self.calls.append(kwargs)
        if self.error: raise self.error
        return type("Response", (), {"parsed": self.output})()


class FakeClient:
    def __init__(self, output=None, error=None): self.models = FakeModels(output, error)


def request(category="TECHNICAL", answer="Spring supplies a class with its dependencies.", previous=None):
    return FollowUpQuestionRequest(original_question="What is dependency injection?", candidate_answer=answer,
        category=category, skill="Spring Boot", difficulty="MEDIUM", previous_followups=previous or [], recent_context=[])


@pytest.mark.parametrize("category,question", [
    ("TECHNICAL", "Why is constructor injection useful?"),
    ("HR", "What specific action did you take?"),
    ("PROJECT", "How did your API communicate with the database?"),
])
def test_generates_structured_followup(category, question):
    output = FollowUpQuestionResponse(should_ask_followup=True, question=question, reason="Useful detail is missing.", focus_area="clarity")
    fake = FakeClient(output)
    result = FollowUpGenerator(Settings(gemini_api_key="test", gemini_model="test"), fake).generate(request(category))
    assert result.question == question
    assert fake.models.calls[0]["config"].response_schema is FollowUpQuestionResponse


def test_blank_answer_skips_provider():
    fake = FakeClient()
    result = FollowUpGenerator(Settings(gemini_api_key="test"), fake).generate(request(answer=" "))
    assert not result.should_ask_followup and not fake.models.calls


def test_limit_skips_provider():
    fake = FakeClient()
    result = FollowUpGenerator(Settings(gemini_api_key="test"), fake).generate(request(previous=["One?", "Two?"]))
    assert not result.should_ask_followup and not fake.models.calls


def test_duplicate_is_suppressed():
    output = FollowUpQuestionResponse(should_ask_followup=True, question="Why constructor injection?", reason="Drill down.")
    result = FollowUpGenerator(Settings(gemini_api_key="test"), FakeClient(output)).generate(request(previous=["Why constructor injection?"]))
    assert not result.should_ask_followup


def test_malformed_output_is_rejected():
    with pytest.raises(FollowUpOutputError):
        FollowUpGenerator(Settings(gemini_api_key="test"), FakeClient({"invalid": True})).generate(request())


def test_provider_unavailable_is_controlled():
    error = httpx.ReadTimeout("timeout", request=httpx.Request("POST", "https://example.test"))
    with pytest.raises(FollowUpUnavailableError):
        FollowUpGenerator(Settings(gemini_api_key="test"), FakeClient(error=error)).generate(request())


def test_endpoint_response_schema(monkeypatch):
    result = FollowUpQuestionResponse(should_ask_followup=False, reason="Complete answer.")
    monkeypatch.setattr(interview_api, "followup_generator", type("Generator", (), {"generate": lambda self, value: result})())
    response = TestClient(app).post("/api/interview/follow-up", json=request().model_dump())
    assert response.status_code == 200
    assert set(response.json()) == set(FollowUpQuestionResponse.model_fields)
