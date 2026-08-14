import json
import httpx
import pytest
from app.core.config import Settings
from app.models.interview import CoachingAnswerItem, CoachingFocusArea, InterviewCoachingRequest, InterviewCoachingResponse, PracticePlanItem
from app.services.coaching_generator import COACHING_NOTE, CoachingGenerator, CoachingOutputError, CoachingUnavailableError, deterministic_signals

def request(**changes):
    item={"question":"Explain HashMap.","answer":"It stores key value pairs.","category":"TECHNICAL","skill":"Java","adaptive":False,"overall_score":55,"relevance_score":20,"correctness_score":18,"completeness_score":8,"communication_score":9,"weaknesses":["Incomplete explanation"],"missing_key_points":["Collision handling"]}
    values={"interview_type":"TECHNICAL","difficulty":"MEDIUM","answers":[CoachingAnswerItem(**item),CoachingAnswerItem(**item)]};values.update(changes);return InterviewCoachingRequest(**values)
def response(): return InterviewCoachingResponse(summary="Completeness needs practice.",primary_focus_areas=[CoachingFocusArea(title="Answer completeness",reason="Repeated low completeness",priority="HIGH",related_skills=["Java"])],practice_recommendations=["Use definition, explanation, example."],revision_topics=["Collision handling"],communication_tips=["Lead with a definition."],next_practice_plan=[PracticePlanItem(order=1,activity="Practice Java questions",focus="Completeness",suggested_question_count=5)],coaching_note="ignored")
class Models:
    def __init__(self,value=None,error=None):self.value=value;self.error=error;self.calls=[]
    def generate_content(self,**kwargs):self.calls.append(kwargs); 
    
class Client:
    def __init__(self,value=None,error=None):
        self.models=Models(value,error)
        def call(**kwargs):
            self.models.calls.append(kwargs)
            if error: raise error
            return type("Result",(),{"parsed":value})()
        self.models.generate_content=call

def test_repeated_weakness_and_missing_concept_are_deterministic():
    signals=deterministic_signals(request());assert signals["evaluated_answer_count"]==2;assert signals["average_scores"]["completeness"]==8;assert signals["repeated_missing_concepts"]==["Collision handling"]

@pytest.mark.parametrize("interview_type",["TECHNICAL","MIXED"])
def test_structured_coaching_and_privacy(interview_type):
    fake=Client(response());result=CoachingGenerator(Settings(gemini_api_key="x",gemini_model="test"),fake).generate(request(interview_type=interview_type));assert result.coaching_note==COACHING_NOTE
    payload=json.loads(fake.models.calls[0]["contents"]);assert not ({"name","email","jwt","resume","user_id"}&set(payload));assert fake.models.calls[0]["config"].response_schema is InterviewCoachingResponse

def test_no_evaluations_is_rejected():
    item=CoachingAnswerItem(question="Q",answer="A",category="HR")
    with pytest.raises(CoachingOutputError,match="Evaluate at least"):CoachingGenerator(Settings(gemini_api_key="x"),Client(response())).generate(request(answers=[item]))

def test_malformed_output_is_rejected():
    with pytest.raises(CoachingOutputError):CoachingGenerator(Settings(gemini_api_key="x"),Client({"bad":True})).generate(request())

def test_provider_unavailable_is_safe():
    error=httpx.ReadTimeout("timeout",request=httpx.Request("POST","https://example.test"))
    with pytest.raises(CoachingUnavailableError):CoachingGenerator(Settings(gemini_api_key="x"),Client(error=error)).generate(request())

def test_request_model_has_no_credentials():
    assert not ({"name","email","jwt","resume","user_id"}&set(InterviewCoachingRequest.model_fields))
