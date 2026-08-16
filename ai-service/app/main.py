import logging
from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from fastapi.encoders import jsonable_encoder

from app.api.resume import router as resume_router
from app.api.interview import router as interview_router

app = FastAPI(
    title="InterviewAce AI Service",
    description="Internal PDF resume extraction service for InterviewAce AI.",
    version="0.1.0",
)
app.include_router(resume_router)
app.include_router(interview_router)

logger = logging.getLogger(__name__)

@app.exception_handler(RequestValidationError)
async def request_validation_error(_: Request, exception: RequestValidationError) -> JSONResponse:
    safe_errors = [{"field": ".".join(str(part) for part in item["loc"]), "reason": item["msg"]} for item in exception.errors()]
    logger.warning("Request validation failed: %s", safe_errors)
    return JSONResponse(status_code=422, content=jsonable_encoder({"detail": exception.errors()}))


@app.get("/health", tags=["Health"], summary="Check AI service health")
def health() -> dict[str, str]:
    return {"status": "UP", "service": "interviewace-ai"}
