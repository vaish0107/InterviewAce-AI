import hashlib
from dataclasses import dataclass

from app.core.questions import GENERAL_TECHNICAL, HR_QUESTIONS, PROJECT_QUESTIONS, TECHNICAL_QUESTIONS
from app.models.interview import InterviewQuestion, QuestionGenerationRequest, QuestionGenerationResponse


@dataclass(frozen=True)
class Candidate:
    question: str
    category: str
    skill: str | None = None


class QuestionGenerator:
    """Generates repeatable, template-based questions without an LLM."""

    def generate(self, request: QuestionGenerationRequest) -> QuestionGenerationResponse:
        technical = self._technical_candidates(request.skills, request.difficulty)
        hr = self._hr_candidates(request.difficulty)
        project = [Candidate(question, "PROJECT") for question in PROJECT_QUESTIONS]

        if request.interview_type == "HR":
            candidates = hr
        elif request.interview_type == "TECHNICAL":
            candidates = self._combine((technical, project), (3, 1), request.question_count)
        else:
            candidates = self._combine((technical, hr, project), (2, 1, 1), request.question_count)

        selected = self._unique(candidates)[: request.question_count]
        if len(selected) < request.question_count:
            fallback = self._unique(technical + hr + project)
            existing = {item.question for item in selected}
            selected.extend(item for item in fallback if item.question not in existing)
            selected = selected[: request.question_count]

        questions = [
            InterviewQuestion(
                id=self._id(request, index, item),
                question=item.question,
                category=item.category,
                skill=item.skill,
                difficulty=request.difficulty,
            )
            for index, item in enumerate(selected, start=1)
        ]
        return QuestionGenerationResponse(
            interview_type=request.interview_type,
            difficulty=request.difficulty,
            total_questions=len(questions),
            questions=questions,
        )

    def _technical_candidates(self, skills: list[str], difficulty: str) -> list[Candidate]:
        recognized: list[str] = []
        seen: set[str] = set()
        for raw_skill in skills:
            canonical = next((name for name in TECHNICAL_QUESTIONS if name.casefold() == raw_skill.strip().casefold()), None)
            if canonical and canonical not in seen:
                recognized.append(canonical)
                seen.add(canonical)

        result: list[Candidate] = []
        skill_questions = [list(TECHNICAL_QUESTIONS[skill][difficulty]) for skill in recognized]
        for question_index in range(max((len(items) for items in skill_questions), default=0)):
            for skill, items in zip(recognized, skill_questions, strict=True):
                if question_index < len(items):
                    result.append(Candidate(items[question_index], "TECHNICAL", skill))
        result.extend(Candidate(question, "TECHNICAL") for question in GENERAL_TECHNICAL[difficulty])
        return self._unique(result)

    def _hr_candidates(self, difficulty: str) -> list[Candidate]:
        order = [difficulty] + [value for value in ("EASY", "MEDIUM", "HARD") if value != difficulty]
        return self._unique([Candidate(question, "HR") for level in order for question in HR_QUESTIONS[level]])

    @staticmethod
    def _combine(pools: tuple[list[Candidate], ...], weights: tuple[int, ...], count: int) -> list[Candidate]:
        positions = [0] * len(pools)
        combined: list[Candidate] = []
        while len(combined) < count and any(positions[index] < len(pool) for index, pool in enumerate(pools)):
            for pool_index, weight in enumerate(weights):
                for _ in range(weight):
                    if positions[pool_index] < len(pools[pool_index]):
                        combined.append(pools[pool_index][positions[pool_index]])
                        positions[pool_index] += 1
                        if len(combined) == count:
                            return combined
        return combined

    @staticmethod
    def _unique(candidates: list[Candidate]) -> list[Candidate]:
        seen: set[str] = set()
        result: list[Candidate] = []
        for candidate in candidates:
            key = candidate.question.casefold()
            if key not in seen:
                seen.add(key)
                result.append(candidate)
        return result

    @staticmethod
    def _id(request: QuestionGenerationRequest, index: int, candidate: Candidate) -> str:
        value = f"{request.interview_type}|{request.difficulty}|{index}|{candidate.category}|{candidate.skill}|{candidate.question}"
        return hashlib.sha256(value.encode("utf-8")).hexdigest()[:16]
