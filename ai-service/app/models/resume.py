from pydantic import BaseModel, Field


class ResumeExtractionResponse(BaseModel):
    file_name: str
    character_count: int = Field(ge=1)
    page_count: int = Field(ge=1)
    extracted_text: str


class SkillExtractionRequest(BaseModel):
    text: str


class SkillExtractionResponse(BaseModel):
    total_skills: int = Field(ge=0)
    skills: list[str]
    categories: dict[str, list[str]]


class AtsSectionScores(BaseModel):
    technical_skills: int = Field(ge=0, le=30)
    projects: int = Field(ge=0, le=20)
    education: int = Field(ge=0, le=15)
    contact_information: int = Field(ge=0, le=10)
    experience: int = Field(ge=0, le=10)
    certifications: int = Field(ge=0, le=5)
    completeness: int = Field(ge=0, le=10)


class AtsAnalysisResponse(BaseModel):
    ats_score: int = Field(ge=0, le=100)
    grade: str
    section_scores: AtsSectionScores
    detected_skills: list[str]
    strengths: list[str]
    weaknesses: list[str]
    recommendations: list[str]
    scoring_note: str


class ExtractAndSkillsResponse(BaseModel):
    file_name: str
    page_count: int = Field(ge=1)
    character_count: int = Field(ge=1)
    skills: list[str]
    categories: dict[str, list[str]]


class BasicResumeAnalysisResponse(AtsAnalysisResponse):
    file_name: str
    page_count: int = Field(ge=1)
    character_count: int = Field(ge=1)


class JobMatchRequest(BaseModel):
    resume_text: str
    job_description: str


class CategoryMatch(BaseModel):
    required_skills: list[str]
    matched_skills: list[str]
    missing_skills: list[str]
    match_percentage: int = Field(ge=0, le=100)


class JobMatchResponse(BaseModel):
    match_percentage: int = Field(ge=0, le=100)
    resume_skill_count: int = Field(ge=0)
    job_skill_count: int = Field(ge=0)
    matched_skills: list[str]
    missing_skills: list[str]
    additional_resume_skills: list[str]
    category_matches: dict[str, CategoryMatch]
    strengths: list[str]
    recommendations: list[str]
    matching_note: str


class PdfJobMatchResponse(JobMatchResponse):
    file_name: str
    page_count: int = Field(ge=1)
    character_count: int = Field(ge=1)
    resume_skills: list[str]
    job_skills: list[str]
