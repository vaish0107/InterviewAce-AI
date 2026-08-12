import pytest

from app.services.skill_extractor import SkillExtractor


@pytest.fixture
def extractor() -> SkillExtractor:
    return SkillExtractor()


def test_detects_core_skills_across_categories(extractor: SkillExtractor) -> None:
    result = extractor.extract("Java Spring Boot React PostgreSQL Docker Git REST API")
    assert result.skills == ["Java", "Spring Boot", "REST API", "React", "PostgreSQL", "Docker", "Git"]
    assert result.categories["languages"] == ["Java"]
    assert result.categories["backend"] == ["Spring Boot", "REST API"]
    assert result.categories["frontend"] == ["React"]
    assert result.categories["databases"] == ["PostgreSQL"]
    assert result.categories["cloud_devops"] == ["Docker"]
    assert result.categories["tools"] == ["Git"]


@pytest.mark.parametrize(
    ("text", "expected"),
    [
        ("Node nodejs NodeJS node.js", "Node.js"),
        ("JS js JavaScript", "JavaScript"),
        ("SpringBoot spring boot SPRING BOOT", "Spring Boot"),
        ("React.js reactjs REACT", "React"),
        ("Postgres POSTGRESQL", "PostgreSQL"),
        ("Built RESTful APIs", "REST API"),
    ],
)
def test_aliases_are_case_insensitive_and_deduplicated(
    extractor: SkillExtractor, text: str, expected: str
) -> None:
    result = extractor.extract(text)
    assert result.skills.count(expected) == 1


def test_deduplicates_repeated_canonical_skill(extractor: SkillExtractor) -> None:
    result = extractor.extract("Java java JAVA Java")
    assert result.skills == ["Java"]


def test_does_not_find_c_inside_other_words(extractor: SkillExtractor) -> None:
    result = extractor.extract("Communication cloud architecture React CSS")
    assert "C" not in result.skills
    assert "React" in result.skills
    assert "CSS" in result.skills


def test_finds_standalone_c(extractor: SkillExtractor) -> None:
    assert "C" in extractor.extract("Programming languages: C, C++ and Python").skills
