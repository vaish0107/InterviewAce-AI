from dataclasses import dataclass

from app.services.skill_extractor import ExtractedSkills, SkillExtractor


MATCHING_NOTE = (
    "This match is based on explicitly detected skills and does not measure a "
    "candidate's actual proficiency or overall hiring suitability."
)


@dataclass(frozen=True)
class CategoryMatchResult:
    required_skills: list[str]
    matched_skills: list[str]
    missing_skills: list[str]
    match_percentage: int


@dataclass(frozen=True)
class JobMatchResult:
    match_percentage: int
    resume_skills: list[str]
    job_skills: list[str]
    matched_skills: list[str]
    missing_skills: list[str]
    additional_resume_skills: list[str]
    category_matches: dict[str, CategoryMatchResult]
    strengths: list[str]
    recommendations: list[str]


class JobMatcher:
    """Compare explicitly detected skills using one shared deterministic extractor."""

    def __init__(self, skill_extractor: SkillExtractor | None = None) -> None:
        self._skill_extractor = skill_extractor or SkillExtractor()

    def match(self, resume_text: str, job_description: str) -> JobMatchResult:
        resume = self._skill_extractor.extract(resume_text)
        job = self._skill_extractor.extract(job_description)
        resume_set = set(resume.skills)
        job_set = set(job.skills)
        matched = [skill for skill in job.skills if skill in resume_set]
        missing = [skill for skill in job.skills if skill not in resume_set]
        additional = [skill for skill in resume.skills if skill not in job_set]
        percentage = self._percentage(len(matched), len(job.skills))
        categories = self._category_matches(resume, job)

        return JobMatchResult(
            match_percentage=percentage,
            resume_skills=resume.skills,
            job_skills=job.skills,
            matched_skills=matched,
            missing_skills=missing,
            additional_resume_skills=additional,
            category_matches=categories,
            strengths=self._strengths(percentage, categories, bool(job.skills)),
            recommendations=self._recommendations(percentage, categories, bool(job.skills)),
        )

    @staticmethod
    def _percentage(matched: int, required: int) -> int:
        if required == 0:
            return 0
        return max(0, min(100, round(matched / required * 100)))

    def _category_matches(
        self, resume: ExtractedSkills, job: ExtractedSkills
    ) -> dict[str, CategoryMatchResult]:
        result: dict[str, CategoryMatchResult] = {}
        for category, required in job.categories.items():
            resume_category = set(resume.categories.get(category, []))
            matched = [skill for skill in required if skill in resume_category]
            missing = [skill for skill in required if skill not in resume_category]
            result[category] = CategoryMatchResult(
                required_skills=required,
                matched_skills=matched,
                missing_skills=missing,
                match_percentage=self._percentage(len(matched), len(required)),
            )
        return result

    @staticmethod
    def _strengths(
        percentage: int, categories: dict[str, CategoryMatchResult], has_requirements: bool
    ) -> list[str]:
        strengths: list[str] = []
        if has_requirements and percentage >= 80:
            strengths.append("Strong alignment with the technical skills identified in the job description")
        elif has_requirements and percentage >= 60:
            strengths.append("Good alignment with several required technical skills")
        for category, label in (
            ("backend", "All detected backend requirements are covered"),
            ("databases", "All detected database requirements are covered"),
        ):
            match = categories.get(category)
            if match and not match.missing_skills:
                strengths.append(label)
        return strengths

    @staticmethod
    def _recommendations(
        percentage: int, categories: dict[str, CategoryMatchResult], has_requirements: bool
    ) -> list[str]:
        if not has_requirements:
            return [
                "No supported technical skills were detected in the job description; review it for specific requirements before comparing."
            ]

        recommendations: list[str] = []
        messages = {
            "backend": "The job description includes backend technologies not detected in the resume. Highlight relevant experience if you have it; otherwise consider developing these skills.",
            "cloud_devops": "Consider strengthening cloud/DevOps knowledge for roles with these requirements, and highlight relevant experience if you have it.",
            "testing": "The role mentions testing tools not detected in the resume. Include relevant testing experience if applicable; otherwise consider developing these skills.",
        }
        for category, match in categories.items():
            if match.missing_skills and category in messages:
                recommendations.append(messages[category])
        if percentage < 60:
            recommendations.append(
                "Review the job requirements and focus your resume on relevant skills and experience you genuinely possess."
            )
        return recommendations
