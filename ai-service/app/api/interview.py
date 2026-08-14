from fastapi import APIRouter, HTTPException, status

from app.models.interview import AnswerEvaluationRequest, AnswerEvaluationResponse, FollowUpQuestionRequest, FollowUpQuestionResponse, QuestionGenerationRequest, QuestionGenerationResponse
from app.services.answer_evaluator import AnswerEvaluator, EvaluationConfigurationError, EvaluationOutputError, EvaluationUnavailableError
from app.services.followup_generator import FollowUpConfigurationError, FollowUpGenerator, FollowUpOutputError, FollowUpUnavailableError
from app.services.question_generator import QuestionGenerator
from app.models.interview import InterviewCoachingRequest, InterviewCoachingResponse
from app.services.coaching_generator import CoachingConfigurationError, CoachingGenerator, CoachingOutputError, CoachingUnavailableError

router = APIRouter(prefix="/api/interview", tags=["Interview Practice"])
generator = QuestionGenerator()
evaluator = AnswerEvaluator()
followup_generator = FollowUpGenerator()
coaching_generator = CoachingGenerator()


@router.post("/questions", response_model=QuestionGenerationResponse, summary="Generate deterministic interview questions")
def generate_questions(request: QuestionGenerationRequest) -> QuestionGenerationResponse:
    return generator.generate(request)


@router.post("/evaluate-answer", response_model=AnswerEvaluationResponse, summary="Evaluate one saved interview answer with AI")
def evaluate_answer(request: AnswerEvaluationRequest) -> AnswerEvaluationResponse:
    try:
        return evaluator.evaluate(request)
    except EvaluationConfigurationError as error:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=str(error)) from error
    except EvaluationUnavailableError as error:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=str(error)) from error
    except EvaluationOutputError as error:
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY, detail=str(error)) from error


@router.post("/follow-up", response_model=FollowUpQuestionResponse, summary="Generate one optional adaptive follow-up")
def generate_followup(request: FollowUpQuestionRequest) -> FollowUpQuestionResponse:
    try:
        return followup_generator.generate(request)
    except (FollowUpConfigurationError, FollowUpUnavailableError) as error:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=str(error)) from error
    except FollowUpOutputError as error:
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY, detail=str(error)) from error


@router.post("/coaching", response_model=InterviewCoachingResponse, summary="Generate coaching from saved interview data")
def generate_coaching(request: InterviewCoachingRequest) -> InterviewCoachingResponse:
    try:
        return coaching_generator.generate(request)
    except (CoachingConfigurationError, CoachingUnavailableError) as error:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=str(error)) from error
    except CoachingOutputError as error:
        code = status.HTTP_422_UNPROCESSABLE_ENTITY if "Evaluate at least" in str(error) else status.HTTP_502_BAD_GATEWAY
        raise HTTPException(status_code=code, detail=str(error)) from error
