from fastapi import FastAPI

from app.api.resume import router as resume_router
from app.api.interview import router as interview_router

app = FastAPI(
    title="InterviewAce AI Service",
    description="Internal PDF resume extraction service for InterviewAce AI.",
    version="0.1.0",
)
app.include_router(resume_router)
app.include_router(interview_router)


@app.get("/health", tags=["Health"], summary="Check AI service health")
def health() -> dict[str, str]:
    return {"status": "UP", "service": "interviewace-ai"}
