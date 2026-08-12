import pytest
from fastapi.testclient import TestClient

from app.main import app
from app.models.interview import QuestionGenerationRequest
from app.services.question_generator import QuestionGenerator

client = TestClient(app)
generator = QuestionGenerator()


def request(skills: list[str], interview_type: str = "TECHNICAL", difficulty: str = "MEDIUM", count: int = 6):
    return QuestionGenerationRequest(skills=skills, interview_type=interview_type, difficulty=difficulty, question_count=count)


def test_generates_java_questions() -> None:
    result = generator.generate(request(["Java"]))
    assert any(question.skill == "Java" and "HashMap" in question.question for question in result.questions)


def test_generates_react_questions() -> None:
    result = generator.generate(request(["React"]))
    assert any(question.skill == "React" and "useEffect" in question.question for question in result.questions)


def test_generates_hr_questions_without_skills() -> None:
    result = generator.generate(request([], "HR", "EASY", 5))
    assert result.total_questions == 5
    assert all(question.category == "HR" and question.skill is None for question in result.questions)


def test_mixed_contains_technical_hr_and_project_questions() -> None:
    result = generator.generate(request(["Java"], "MIXED", "MEDIUM", 10))
    assert {question.category for question in result.questions} == {"TECHNICAL", "HR", "PROJECT"}


@pytest.mark.parametrize("difficulty", ["EASY", "MEDIUM", "HARD"])
def test_respects_difficulty(difficulty: str) -> None:
    result = generator.generate(request(["Java"], difficulty=difficulty))
    assert all(question.difficulty == difficulty for question in result.questions)


def test_prevents_duplicates_and_respects_count() -> None:
    result = generator.generate(request(["Java", "Java", "Spring Boot"], "MIXED", "MEDIUM", 20))
    texts = [question.question for question in result.questions]
    assert len(texts) == 20
    assert len(texts) == len(set(texts))


@pytest.mark.parametrize("count", [2, 21])
def test_endpoint_validates_question_count(count: int) -> None:
    response = client.post("/api/interview/questions", json={"skills": [], "interview_type": "HR", "difficulty": "EASY", "question_count": count})
    assert response.status_code == 422


@pytest.mark.parametrize("count", [3, 20])
def test_supports_minimum_and_maximum_count(count: int) -> None:
    result = generator.generate(request([], "HR", "MEDIUM", count))
    assert result.total_questions == count


@pytest.mark.parametrize("skills", [[], ["Unknown Framework"]])
def test_unknown_or_empty_skills_use_general_fallback(skills: list[str]) -> None:
    result = generator.generate(request(skills, "TECHNICAL", "MEDIUM", 5))
    assert result.total_questions == 5
    assert all(question.skill is None for question in result.questions)


def test_same_request_is_deterministic() -> None:
    value = request(["Java", "React"], "MIXED", "HARD", 12)
    assert generator.generate(value).model_dump() == generator.generate(value).model_dump()


def test_endpoint_returns_typed_response() -> None:
    response = client.post("/api/interview/questions", json={"skills": ["Java"], "interview_type": "TECHNICAL", "difficulty": "EASY", "question_count": 3})
    assert response.status_code == 200
    assert response.json()["total_questions"] == 3
