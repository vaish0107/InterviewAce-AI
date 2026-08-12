from app.services.job_matcher import JobMatcher


matcher = JobMatcher()


def test_exact_match_is_complete_and_deterministic() -> None:
    first = matcher.match("Java Spring Boot PostgreSQL", "Java Spring Boot PostgreSQL")
    second = matcher.match("Java Spring Boot PostgreSQL", "Java Spring Boot PostgreSQL")
    assert first == second
    assert first.match_percentage == 100
    assert first.matched_skills == ["Java", "Spring Boot", "PostgreSQL"]
    assert first.missing_skills == []


def test_partial_match_reports_missing_and_additional_skills() -> None:
    result = matcher.match(
        "Java Spring Boot React PostgreSQL Docker Git REST API",
        "Java Spring Boot PostgreSQL REST API Docker AWS Kubernetes",
    )
    assert result.match_percentage == 71
    assert result.matched_skills == ["Java", "Spring Boot", "REST API", "PostgreSQL", "Docker"]
    assert result.missing_skills == ["Kubernetes", "AWS"]
    assert result.additional_resume_skills == ["React", "Git"]


def test_zero_match_and_score_bounds() -> None:
    result = matcher.match("React Git", "Java PostgreSQL Docker")
    assert result.match_percentage == 0
    assert 0 <= result.match_percentage <= 100
    assert result.matched_skills == []


def test_category_matches_only_include_job_categories() -> None:
    result = matcher.match("Spring Boot React PostgreSQL", "Spring Boot REST API PostgreSQL Docker")
    assert set(result.category_matches) == {"backend", "databases", "cloud_devops"}
    assert result.category_matches["backend"].match_percentage == 50
    assert result.category_matches["databases"].match_percentage == 100
    assert result.category_matches["cloud_devops"].match_percentage == 0
    assert "All detected database requirements are covered" in result.strengths


def test_extractor_handles_duplicates_case_and_aliases() -> None:
    result = matcher.match("JAVA java SpringBoot POSTGRES k8s", "Java Spring Boot PostgreSQL Kubernetes")
    assert result.match_percentage == 100
    assert result.resume_skills.count("Java") == 1


def test_no_recognizable_job_skills_does_not_fabricate_requirements() -> None:
    result = matcher.match("Java Docker", "Friendly colleague with excellent communication")
    assert result.match_percentage == 0
    assert result.job_skills == []
    assert result.matched_skills == []
    assert result.missing_skills == []
    assert result.category_matches == {}
    assert "No supported technical skills were detected" in result.recommendations[0]


def test_recommendations_are_honest_and_evidence_based() -> None:
    result = matcher.match("Java", "Java Spring Boot AWS Kubernetes Pytest")
    combined = " ".join(result.recommendations).lower()
    assert "add aws to your resume" not in combined
    assert "genuinely possess" in combined
    assert "if you have it" in combined or "if applicable" in combined
