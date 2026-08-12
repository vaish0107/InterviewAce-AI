from pathlib import Path
from dataclasses import asdict

from fastapi import APIRouter, File, Form, HTTPException, UploadFile, status

from app.core.config import get_settings
from app.models.resume import (
    AtsAnalysisResponse,
    BasicResumeAnalysisResponse,
    ExtractAndSkillsResponse,
    JobMatchRequest,
    JobMatchResponse,
    PdfJobMatchResponse,
    ResumeExtractionResponse,
    SkillExtractionRequest,
    SkillExtractionResponse,
)
from app.services.ats_scorer import SCORING_NOTE, AtsResult, AtsScorer
from app.services.pdf_extractor import (
    EmptyPdfTextError,
    EncryptedPdfError,
    InvalidPdfError,
    PdfTooLargeError,
    UnreadablePdfError,
    extract_pdf_text,
)
from app.services.skill_extractor import SkillExtractor
from app.services.job_matcher import MATCHING_NOTE, JobMatchResult, JobMatcher

router = APIRouter(prefix="/api/resume", tags=["Resume Extraction"])
skill_extractor = SkillExtractor()
ats_scorer = AtsScorer()
job_matcher = JobMatcher(skill_extractor)


@router.post("/job-match", response_model=JobMatchResponse, summary="Match resume skills to a job description")
def match_job(request: JobMatchRequest) -> JobMatchResponse:
    if not request.resume_text.strip():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Resume text must not be blank.")
    if not request.job_description.strip():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Job description must not be blank.")
    return _job_match_response(job_matcher.match(request.resume_text, request.job_description))


@router.post(
    "/analyze-job-match",
    response_model=PdfJobMatchResponse,
    summary="Extract a PDF resume and match it to a job description",
)
async def analyze_job_match(
    file: UploadFile = File(...), job_description: str = Form(...)
) -> PdfJobMatchResponse:
    if not job_description.strip():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Job description must not be blank.")
    file_name, extracted = await _extract_uploaded_pdf(file)
    result = job_matcher.match(extracted.text, job_description)
    response = _job_match_response(result).model_dump()
    return PdfJobMatchResponse(
        file_name=file_name,
        page_count=extracted.page_count,
        character_count=len(extracted.text),
        resume_skills=result.resume_skills,
        job_skills=result.job_skills,
        **response,
    )


@router.post(
    "/skills",
    response_model=SkillExtractionResponse,
    summary="Extract technical skills from resume text",
    description=(
        "Deterministically detects normalized technical skills and common aliases in "
        "previously extracted resume text. No PDF upload, LLM, or ATS scoring is involved."
    ),
)
def extract_skills(request: SkillExtractionRequest) -> SkillExtractionResponse:
    if not request.text.strip():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Resume text must not be blank.")
    extracted = skill_extractor.extract(request.text)
    return SkillExtractionResponse(
        total_skills=len(extracted.skills),
        skills=extracted.skills,
        categories=extracted.categories,
    )


@router.post(
    "/ats-score",
    response_model=AtsAnalysisResponse,
    summary="Calculate a deterministic ATS resume-quality score",
    description="Scores extracted resume text using documented, capped heuristics only. No LLM or external API is used.",
)
def score_resume(request: SkillExtractionRequest) -> AtsAnalysisResponse:
    if not request.text.strip():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Resume text must not be blank.")
    skills = skill_extractor.extract(request.text)
    result = ats_scorer.score(request.text, skills.skills)
    return _ats_response(result, skills.skills)


@router.post(
    "/extract",
    response_model=ResumeExtractionResponse,
    summary="Extract text from a PDF resume",
    description=(
        "Accepts a PDF-only multipart field named `file` (maximum 5 MB by default), "
        "extracts readable text in memory, and does not persist the upload. This internal "
        "endpoint has no authentication until Spring Boot service integration is added."
    ),
)
async def extract_resume(file: UploadFile = File(...)) -> ResumeExtractionResponse:
    file_name, extracted = await _extract_uploaded_pdf(file)
    return ResumeExtractionResponse(
        file_name=file_name,
        character_count=len(extracted.text),
        page_count=extracted.page_count,
        extracted_text=extracted.text,
    )


@router.post(
    "/extract-and-skills",
    response_model=ExtractAndSkillsResponse,
    summary="Extract PDF text and detect technical skills",
    description="Validates and extracts a PDF in memory, then applies the deterministic skill catalog.",
)
async def extract_and_skills(file: UploadFile = File(...)) -> ExtractAndSkillsResponse:
    file_name, extracted = await _extract_uploaded_pdf(file)
    skills = skill_extractor.extract(extracted.text)
    return ExtractAndSkillsResponse(
        file_name=file_name,
        page_count=extracted.page_count,
        character_count=len(extracted.text),
        skills=skills.skills,
        categories=skills.categories,
    )


@router.post(
    "/analyze-basic",
    response_model=BasicResumeAnalysisResponse,
    summary="Extract and deterministically analyze a PDF resume",
    description="Combines in-memory PDF extraction, skill detection, and explainable ATS heuristics without an LLM.",
)
async def analyze_basic(file: UploadFile = File(...)) -> BasicResumeAnalysisResponse:
    file_name, extracted = await _extract_uploaded_pdf(file)
    skills = skill_extractor.extract(extracted.text)
    result = ats_scorer.score(extracted.text, skills.skills)
    analysis = _ats_response(result, skills.skills).model_dump()
    return BasicResumeAnalysisResponse(
        file_name=file_name,
        page_count=extracted.page_count,
        character_count=len(extracted.text),
        **analysis,
    )


async def _extract_uploaded_pdf(file: UploadFile):
    file_name = _safe_file_name(file.filename)
    if Path(file_name).suffix.lower() != ".pdf":
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Only PDF files are supported.")
    if file.content_type and file.content_type.lower() != "application/pdf":
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="File MIME type must be application/pdf.")

    settings = get_settings()
    contents = await file.read(settings.max_resume_size + 1)
    await file.close()
    try:
        extracted = extract_pdf_text(contents, settings.max_resume_size)
    except PdfTooLargeError as error:
        raise HTTPException(status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE, detail=str(error)) from error
    except InvalidPdfError as error:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(error)) from error
    except (EncryptedPdfError, UnreadablePdfError, EmptyPdfTextError) as error:
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail=str(error)) from error

    return file_name, extracted


def _ats_response(result: AtsResult, skills: list[str]) -> AtsAnalysisResponse:
    return AtsAnalysisResponse(
        ats_score=result.score,
        grade=result.grade,
        section_scores=asdict(result.section_scores),
        detected_skills=skills,
        strengths=result.strengths,
        weaknesses=result.weaknesses,
        recommendations=result.recommendations,
        scoring_note=SCORING_NOTE,
    )


def _job_match_response(result: JobMatchResult) -> JobMatchResponse:
    return JobMatchResponse(
        match_percentage=result.match_percentage,
        resume_skill_count=len(result.resume_skills),
        job_skill_count=len(result.job_skills),
        matched_skills=result.matched_skills,
        missing_skills=result.missing_skills,
        additional_resume_skills=result.additional_resume_skills,
        category_matches={name: asdict(value) for name, value in result.category_matches.items()},
        strengths=result.strengths,
        recommendations=result.recommendations,
        matching_note=MATCHING_NOTE,
    )


def _safe_file_name(file_name: str | None) -> str:
    if not file_name:
        return "resume.pdf"
    safe_name = file_name.replace("\\", "/").rsplit("/", maxsplit=1)[-1].strip()
    return safe_name or "resume.pdf"
