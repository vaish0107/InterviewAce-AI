import re
from dataclasses import dataclass


SCORING_NOTE = "This score is a heuristic resume-quality indicator and is not an official ATS vendor score."


@dataclass(frozen=True)
class AtsScores:
    technical_skills: int
    projects: int
    education: int
    contact_information: int
    experience: int
    certifications: int
    completeness: int

    @property
    def total(self) -> int:
        return min(100, sum((
            self.technical_skills,
            self.projects,
            self.education,
            self.contact_information,
            self.experience,
            self.certifications,
            self.completeness,
        )))


@dataclass(frozen=True)
class AtsResult:
    score: int
    grade: str
    section_scores: AtsScores
    strengths: list[str]
    weaknesses: list[str]
    recommendations: list[str]


@dataclass(frozen=True)
class ResumeSignals:
    has_email: bool
    has_phone: bool
    has_linkedin: bool
    has_github_or_portfolio: bool
    has_projects: bool
    has_project_technology: bool
    has_substantial_projects: bool
    has_education: bool
    has_detailed_education: bool
    experience_indications: int
    certification_indications: int
    section_count: int


class AtsScorer:
    EMAIL_PATTERN = re.compile(r"\b[a-z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-z0-9-]+(?:\.[a-z0-9-]+)+\b", re.I)
    PHONE_PATTERN = re.compile(r"(?<!\d)(?=(?:[\s().-]*\d){10,15}(?!\d))\+?\d[\d\s().-]*\d(?!\d)")
    LINKEDIN_PATTERN = re.compile(r"(?:https?://)?(?:www\.)?linkedin\.com/(?:in|pub)/[a-z0-9_-]+", re.I)
    GITHUB_PORTFOLIO_PATTERN = re.compile(
        r"(?:https?://)?(?:www\.)?github\.com/[a-z0-9_-]+|\bportfolio\b|\bpersonal website\b",
        re.I,
    )
    PROJECT_PATTERN = re.compile(r"\b(?:academic projects?|personal projects?|project experience|projects?)\b", re.I)
    EDUCATION_PATTERN = re.compile(
        r"\b(?:education|university|college|bachelor(?: of technology)?|b\.?\s?tech|degree|engineering|master|m\.?\s?tech|diploma)\b",
        re.I,
    )
    DEGREE_PATTERN = re.compile(r"\b(?:bachelor(?: of technology)?|b\.?\s?tech|degree|engineering|master|m\.?\s?tech|diploma)\b", re.I)
    INSTITUTION_PATTERN = re.compile(r"\b(?:university|college|institute|school)\b", re.I)
    EXPERIENCE_PATTERN = re.compile(r"\b(?:professional experience|work experience|experience|employment|internship|intern)\b", re.I)
    CERTIFICATION_PATTERN = re.compile(
        r"\b(?:certifications?|certified|certificate|coursera|udemy|nptel|oracle certified|aws certified|microsoft certified)\b",
        re.I,
    )
    SUMMARY_PATTERN = re.compile(r"\b(?:professional summary|summary|career objective|objective|profile)\b", re.I)
    SKILLS_PATTERN = re.compile(r"\b(?:technical skills|skills|technologies|tech stack)\b", re.I)
    ACHIEVEMENTS_PATTERN = re.compile(r"\b(?:achievements?|awards?|honors?)\b", re.I)

    def score(self, text: str, detected_skills: list[str]) -> AtsResult:
        normalized = re.sub(r"\s+", " ", text).strip()
        signals = self._signals(normalized, detected_skills)
        scores = AtsScores(
            technical_skills=self.technical_skill_score(len(detected_skills)),
            projects=self._project_score(signals),
            education=self._education_score(signals),
            contact_information=self._contact_score(signals),
            experience=self._experience_score(signals),
            certifications=self._certification_score(signals),
            completeness=self.completeness_score(signals.section_count),
        )
        return AtsResult(
            score=scores.total,
            grade=self.grade_for(scores.total),
            section_scores=scores,
            strengths=self._strengths(scores),
            weaknesses=self._weaknesses(scores, signals),
            recommendations=self._recommendations(scores, signals),
        )

    @staticmethod
    def technical_skill_score(skill_count: int) -> int:
        if skill_count == 0: return 0
        if skill_count <= 2: return 8
        if skill_count <= 4: return 15
        if skill_count <= 7: return 22
        if skill_count <= 10: return 26
        return 30

    @staticmethod
    def completeness_score(section_count: int) -> int:
        if section_count <= 1: return 2
        if section_count <= 3: return 4
        if section_count <= 5: return 7
        return 10

    @staticmethod
    def grade_for(score: int) -> str:
        if score >= 90: return "A+"
        if score >= 80: return "A"
        if score >= 70: return "B"
        if score >= 60: return "C"
        if score >= 50: return "D"
        return "Needs Improvement"

    def _signals(self, text: str, skills: list[str]) -> ResumeSignals:
        project_matches = list(self.PROJECT_PATTERN.finditer(text))
        project_content = text[project_matches[0].start():] if project_matches else ""
        project_skill_count = sum(1 for skill in skills if re.search(rf"(?<!\w){re.escape(skill)}(?!\w)", project_content, re.I))
        project_word_count = len(project_content.split())
        experience_count = len(self.EXPERIENCE_PATTERN.findall(text))
        certification_count = len(self.CERTIFICATION_PATTERN.findall(text))
        has_education = bool(self.EDUCATION_PATTERN.search(text))
        section_flags = (
            bool(self.SUMMARY_PATTERN.search(text)),
            bool(self.SKILLS_PATTERN.search(text)) or bool(skills),
            has_education,
            bool(project_matches),
            experience_count > 0,
            certification_count > 0,
            bool(self.ACHIEVEMENTS_PATTERN.search(text)),
        )
        return ResumeSignals(
            has_email=bool(self.EMAIL_PATTERN.search(text)),
            has_phone=bool(self.PHONE_PATTERN.search(text)),
            has_linkedin=bool(self.LINKEDIN_PATTERN.search(text)),
            has_github_or_portfolio=bool(self.GITHUB_PORTFOLIO_PATTERN.search(text)),
            has_projects=bool(project_matches),
            has_project_technology=project_skill_count > 0,
            has_substantial_projects=len(project_matches) >= 2 or (project_word_count >= 50 and project_skill_count >= 2),
            has_education=has_education,
            has_detailed_education=has_education and bool(self.DEGREE_PATTERN.search(text))
                and bool(self.INSTITUTION_PATTERN.search(text) or re.search(r"\beducation\b", text, re.I)),
            experience_indications=experience_count,
            certification_indications=certification_count,
            section_count=sum(section_flags),
        )

    @staticmethod
    def _project_score(signals: ResumeSignals) -> int:
        if not signals.has_projects: return 0
        if signals.has_substantial_projects: return 20
        if signals.has_project_technology: return 14
        return 8

    @staticmethod
    def _education_score(signals: ResumeSignals) -> int:
        if not signals.has_education: return 0
        return 15 if signals.has_detailed_education else 8

    @staticmethod
    def _contact_score(signals: ResumeSignals) -> int:
        return (4 * signals.has_email + 3 * signals.has_phone + 2 * signals.has_linkedin
                + 1 * signals.has_github_or_portfolio)

    @staticmethod
    def _experience_score(signals: ResumeSignals) -> int:
        if signals.experience_indications == 0: return 0
        return 10 if signals.experience_indications >= 2 else 5

    @staticmethod
    def _certification_score(signals: ResumeSignals) -> int:
        if signals.certification_indications == 0: return 0
        return 5 if signals.certification_indications >= 2 else 3

    @staticmethod
    def _strengths(scores: AtsScores) -> list[str]:
        strengths: list[str] = []
        if scores.technical_skills >= 22: strengths.append("Strong technical skill coverage")
        if scores.projects >= 14: strengths.append("Projects demonstrate practical implementation experience")
        if scores.education == 15: strengths.append("Education details are clearly presented")
        if scores.contact_information >= 8: strengths.append("Contact and professional profile information is well covered")
        if scores.experience >= 5: strengths.append("Relevant experience or internship information is included")
        if scores.completeness >= 7: strengths.append("Resume includes most important sections")
        return strengths

    @staticmethod
    def _weaknesses(scores: AtsScores, signals: ResumeSignals) -> list[str]:
        weaknesses: list[str] = []
        if scores.technical_skills == 0: weaknesses.append("Technical skills section is missing or unclear")
        elif scores.technical_skills <= 8: weaknesses.append("Technical skill coverage is limited")
        if scores.projects == 0: weaknesses.append("Projects are not clearly presented")
        if scores.education == 0: weaknesses.append("Education information is missing")
        if not signals.has_email: weaknesses.append("Email address could not be detected")
        if not signals.has_linkedin: weaknesses.append("LinkedIn profile is not included")
        if not signals.has_github_or_portfolio: weaknesses.append("GitHub or portfolio link is not included")
        if scores.experience == 0: weaknesses.append("No internship or work experience information was detected")
        if scores.certifications == 0: weaknesses.append("No certifications were detected")
        if scores.projects == 8: weaknesses.append("Projects lack clear technology and implementation details")
        return weaknesses

    @staticmethod
    def _recommendations(scores: AtsScores, signals: ResumeSignals) -> list[str]:
        recommendations: list[str] = []
        if scores.technical_skills <= 8:
            recommendations.append("Create a dedicated technical skills section and group skills by category.")
        if scores.projects == 0:
            recommendations.append("Add 2–3 relevant projects and describe the technologies and results.")
        elif scores.projects == 8:
            recommendations.append("Add concise project descriptions highlighting your contribution, technology stack, and outcome.")
        if scores.education == 0:
            recommendations.append("Add an education section with your degree and institution.")
        if not signals.has_email:
            recommendations.append("Add a professional email address.")
        if not signals.has_linkedin:
            recommendations.append("Add a professional LinkedIn profile link.")
        if not signals.has_github_or_portfolio:
            recommendations.append("Add a GitHub or portfolio link to showcase projects.")
        if scores.experience == 0:
            recommendations.append("Add relevant internship, employment, or practical experience if applicable.")
        if scores.certifications == 0:
            recommendations.append("Add relevant certifications if you have completed any.")
        if scores.completeness <= 4:
            recommendations.append("Use clearly labeled sections such as Skills, Education, Projects, and Experience.")
        return recommendations
