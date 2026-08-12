from fastapi import APIRouter, HTTPException, status

from app.models.interview import AnswerEvaluationRequest, AnswerEvaluationResponse, QuestionGenerationRequest, QuestionGenerationResponse
from app.services.answer_evaluator import AnswerEvaluator, EvaluationConfigurationError, EvaluationOutputError, EvaluationUnavailableError
from app.services.question_generator import QuestionGenerator

router = APIRouter(prefix="/api/interview", tags=["Interview Practice"])
generator = QuestionGenerator()
evaluator = AnswerEvaluator()


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
