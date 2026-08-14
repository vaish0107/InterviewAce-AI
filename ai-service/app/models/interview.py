from pydantic import BaseModel, Field, field_validator


class InterviewQuestion(BaseModel):
    id: str
    question: str
    category: str
    skill: str | None = None
    difficulty: str


class QuestionGenerationRequest(BaseModel):
    skills: list[str] = Field(default_factory=list)
    interview_type: str
    difficulty: str
    question_count: int = Field(ge=3, le=20)

    @field_validator("interview_type")
    @classmethod
    def valid_type(cls, value: str) -> str:
        normalized = value.strip().upper()
        if normalized not in {"TECHNICAL", "HR", "MIXED"}:
            raise ValueError("interview_type must be TECHNICAL, HR, or MIXED")
        return normalized

    @field_validator("difficulty")
    @classmethod
    def valid_difficulty(cls, value: str) -> str:
        normalized = value.strip().upper()
        if normalized not in {"EASY", "MEDIUM", "HARD"}:
            raise ValueError("difficulty must be EASY, MEDIUM, or HARD")
        return normalized


class QuestionGenerationResponse(BaseModel):
    interview_type: str
    difficulty: str
    total_questions: int
    questions: list[InterviewQuestion]


class AnswerEvaluationRequest(BaseModel):
    question: str = Field(min_length=1)
    answer: str = Field(min_length=10)
    category: str
    skill: str | None = None
    difficulty: str

    @field_validator("question", "answer")
    @classmethod
    def not_blank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("value must not be blank")
        return value.strip()

    @field_validator("category")
    @classmethod
    def valid_category(cls, value: str) -> str:
        normalized = value.strip().upper()
        if normalized not in {"TECHNICAL", "HR", "PROJECT"}:
            raise ValueError("category must be TECHNICAL, HR, or PROJECT")
        return normalized

    @field_validator("difficulty")
    @classmethod
    def evaluation_difficulty(cls, value: str) -> str:
        normalized = value.strip().upper()
        if normalized not in {"EASY", "MEDIUM", "HARD"}:
            raise ValueError("difficulty must be EASY, MEDIUM, or HARD")
        return normalized


class AnswerEvaluationResponse(BaseModel):
    overall_score: int = Field(ge=0, le=100)
    relevance_score: int = Field(ge=0, le=25)
    correctness_score: int = Field(ge=0, le=35)
    completeness_score: int = Field(ge=0, le=25)
    communication_score: int = Field(ge=0, le=15)
    strengths: list[str]
    weaknesses: list[str]
    missing_key_points: list[str]
    improved_answer: str
    summary: str
    evaluation_note: str


class InterviewContextItem(BaseModel):
    question: str
    answer: str


class FollowUpQuestionRequest(BaseModel):
    original_question: str
    candidate_answer: str
    category: str
    skill: str | None = None
    difficulty: str
    previous_followups: list[str] = Field(default_factory=list)
    recent_context: list[InterviewContextItem] = Field(default_factory=list, max_length=3)


class FollowUpQuestionResponse(BaseModel):
    should_ask_followup: bool
    question: str | None = None
    reason: str
    focus_area: str | None = None


class CoachingAnswerItem(BaseModel):
    question: str
    answer: str
    category: str
    skill: str | None = None
    adaptive: bool = False
    overall_score: int | None = Field(default=None, ge=0, le=100)
    relevance_score: int | None = Field(default=None, ge=0, le=25)
    correctness_score: int | None = Field(default=None, ge=0, le=35)
    completeness_score: int | None = Field(default=None, ge=0, le=25)
    communication_score: int | None = Field(default=None, ge=0, le=15)
    weaknesses: list[str] = Field(default_factory=list)
    missing_key_points: list[str] = Field(default_factory=list)


class InterviewCoachingRequest(BaseModel):
    interview_type: str
    difficulty: str
    answers: list[CoachingAnswerItem] = Field(min_length=1, max_length=20)


class CoachingFocusArea(BaseModel):
    title: str
    reason: str
    priority: str
    related_skills: list[str] = Field(default_factory=list)


class PracticePlanItem(BaseModel):
    order: int = Field(ge=1)
    activity: str
    focus: str
    suggested_question_count: int = Field(ge=1, le=50)


class InterviewCoachingResponse(BaseModel):
    summary: str
    primary_focus_areas: list[CoachingFocusArea]
    practice_recommendations: list[str]
    revision_topics: list[str]
    communication_tips: list[str]
    next_practice_plan: list[PracticePlanItem]
    coaching_note: str
