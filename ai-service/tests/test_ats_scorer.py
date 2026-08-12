import pytest

from app.services.ats_scorer import AtsScorer


@pytest.fixture
def scorer() -> AtsScorer:
    return AtsScorer()


@pytest.mark.parametrize(
    ("count", "score"),
    [(0, 0), (1, 8), (2, 8), (3, 15), (4, 15), (5, 22), (7, 22), (8, 26), (10, 26), (11, 30)],
)
def test_technical_skill_score_boundaries(count: int, score: int) -> None:
    assert AtsScorer.technical_skill_score(count) == score


@pytest.mark.parametrize(
    ("score", "grade"),
    [(100, "A+"), (90, "A+"), (89, "A"), (80, "A"), (79, "B"), (70, "B"),
     (69, "C"), (60, "C"), (59, "D"), (50, "D"), (49, "Needs Improvement")],
)
def test_grade_boundaries(score: int, grade: str) -> None:
    assert AtsScorer.grade_for(score) == grade


def test_contact_signals_are_scored_independently(scorer: AtsScorer) -> None:
    text = "Email jane@example.com Phone +91 98765 43210 LinkedIn linkedin.com/in/jane GitHub github.com/jane"
    result = scorer.score(text, [])
    assert result.section_scores.contact_information == 10


def test_project_technology_education_internship_and_certification_detection(scorer: AtsScorer) -> None:
    text = (
        "Education B.Tech, Example University. Projects Inventory System using Java and PostgreSQL. "
        "Internship Software Developer Intern. Certifications Coursera Java Certificate."
    )
    result = scorer.score(text, ["Java", "PostgreSQL"])
    assert result.section_scores.projects == 14
    assert result.section_scores.education == 15
    assert result.section_scores.experience == 10
    assert result.section_scores.certifications == 5


@pytest.mark.parametrize(
    ("sections", "expected"),
    [(0, 2), (1, 2), (2, 4), (3, 4), (4, 7), (5, 7), (6, 10), (7, 10)],
)
def test_completeness_boundaries(sections: int, expected: int) -> None:
    assert AtsScorer.completeness_score(sections) == expected


def test_score_is_deterministic_and_never_exceeds_100(scorer: AtsScorer) -> None:
    text = (
        "Professional Summary Skills Education B.Tech Example University Projects Project A Java Docker. "
        "Project B React PostgreSQL. Professional Experience Internship. Certifications AWS Certified Coursera. "
        "Achievements. jane@example.com +1 202-555-0147 linkedin.com/in/jane github.com/jane"
    )
    skills = [f"Skill {index}" for index in range(20)]
    first = scorer.score(text, skills)
    second = scorer.score(text, skills)
    assert first == second
    assert 0 <= first.score <= 100


def test_weak_resume_has_supported_weaknesses_and_recommendations(scorer: AtsScorer) -> None:
    result = scorer.score("Motivated candidate seeking an opportunity.", [])
    assert "Technical skills section is missing or unclear" in result.weaknesses
    assert "Projects are not clearly presented" in result.weaknesses
    assert "Education information is missing" in result.weaknesses
    assert "Email address could not be detected" in result.weaknesses
    assert "Add 2–3 relevant projects and describe the technologies and results." in result.recommendations
    assert not result.strengths


def test_strong_resume_has_evidence_based_strengths(scorer: AtsScorer) -> None:
    text = (
        "Summary Skills Java Python React Docker PostgreSQL Git REST API. Education B.Tech Example University. "
        "Projects Project A using Java React Docker PostgreSQL with measured outcomes. Experience Internship. "
        "Certifications AWS Certified. jane@example.com +1 202-555-0147 linkedin.com/in/jane github.com/jane"
    )
    result = scorer.score(text, ["Java", "Python", "React", "Docker", "PostgreSQL", "Git", "REST API"])
    assert "Strong technical skill coverage" in result.strengths
    assert "Projects demonstrate practical implementation experience" in result.strengths
    assert "Education details are clearly presented" in result.strengths
    assert "Contact and professional profile information is well covered" in result.strengths
    assert "Relevant experience or internship information is included" in result.strengths
    assert "Resume includes most important sections" in result.strengths
