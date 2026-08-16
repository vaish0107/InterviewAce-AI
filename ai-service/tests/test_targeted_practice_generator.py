import httpx
import pytest
from app.core.config import Settings
from app.models.interview import TargetedPracticeQuestion, TargetedPracticeRequest, TargetedPracticeResponse
from app.services.targeted_practice_generator import TargetedPracticeGenerator, TargetedPracticeOutputError, TargetedPracticeUnavailableError

class Client:
    def __init__(self,value=None,error=None):
        class Models: pass
        self.models=Models(); self.calls=[]
        def generate_content(**kwargs):
            self.calls.append(kwargs)
            if error: raise error
            return type("Result",(),{"parsed":value})()
        self.models.generate_content=generate_content
def request(**changes):
    values={"focus_area":"Java Collections","skill":"Java","difficulty":"MEDIUM","question_count":5,"weakness_context":"Needs depth around maps","avoid_questions":[]};values.update(changes);return TargetedPracticeRequest(**values)
def response(count=5,difficulty="MEDIUM",skill="Java"):
    return TargetedPracticeResponse(questions=[TargetedPracticeQuestion(question=f"Focused question {i}?",category="TECHNICAL",skill=skill,difficulty=difficulty,focus_concept=f"Concept {i}") for i in range(count)])

@pytest.mark.parametrize(("focus,skill"),[("Java Collections","Java"),("React state","React"),("Clear communication",None)])
def test_weakness_types_are_generated(focus,skill):
    result=TargetedPracticeGenerator(Settings(gemini_api_key="x"),Client(response(skill=skill))).generate(request(focus_area=focus,skill=skill));assert len(result.questions)==5
@pytest.mark.parametrize("difficulty",["EASY","MEDIUM","HARD"])
def test_difficulty_is_preserved(difficulty):
    result=TargetedPracticeGenerator(Settings(gemini_api_key="x"),Client(response(difficulty=difficulty))).generate(request(difficulty=difficulty));assert all(q.difficulty==difficulty for q in result.questions)
@pytest.mark.parametrize("count",[3,5])
def test_supported_question_counts(count):
    assert len(TargetedPracticeGenerator(Settings(gemini_api_key="x"),Client(response(count=count))).generate(request(question_count=count)).questions)==count
def test_duplicate_or_avoided_questions_are_rejected():
    duplicate=response();duplicate.questions[1]=duplicate.questions[0]
    with pytest.raises(TargetedPracticeOutputError):TargetedPracticeGenerator(Settings(gemini_api_key="x"),Client(duplicate)).generate(request())
def test_malformed_response_is_rejected():
    with pytest.raises(TargetedPracticeOutputError):TargetedPracticeGenerator(Settings(gemini_api_key="x"),Client({"bad":True})).generate(request())
def test_provider_unavailable_is_controlled():
    error=httpx.ReadTimeout("timeout",request=httpx.Request("POST","https://example.test"))
    with pytest.raises(TargetedPracticeUnavailableError):TargetedPracticeGenerator(Settings(gemini_api_key="x"),Client(error=error)).generate(request())

def test_realistic_react_hook_request_returns_five_usable_questions():
    questions = TargetedPracticeResponse(questions=[TargetedPracticeQuestion(
        question=f"React hook lifecycle timing question {index}?", category="technical",
        skill="React", difficulty="medium", focus_concept=f"Hook execution phase {index}"
    ) for index in range(1, 6)])
    result = TargetedPracticeGenerator(
        Settings(gemini_api_key="x", gemini_model="test-model"), Client(questions)
    ).generate(request(focus_area="React Hook Lifecycle and Execution Timing", skill="React",
                       difficulty="MEDIUM", question_count=5))
    assert len(result.questions) == 5
    assert all(item.question.strip() and item.focus_concept.strip() for item in result.questions)
    assert all(item.category == "TECHNICAL" and item.difficulty == "MEDIUM" for item in result.questions)
