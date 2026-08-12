import pymupdf
from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health_endpoint() -> None:
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "UP", "service": "interviewace-ai"}


def test_rejects_non_pdf_upload() -> None:
    response = client.post(
        "/api/resume/extract",
        files={"file": ("resume.txt", b"not a PDF", "text/plain")},
    )
    assert response.status_code == 400
    assert response.json()["detail"] == "Only PDF files are supported."


def test_rejects_empty_pdf() -> None:
    response = client.post(
        "/api/resume/extract",
        files={"file": ("resume.pdf", b"", "application/pdf")},
    )
    assert response.status_code == 400
    assert response.json()["detail"] == "PDF file must not be empty."


def test_extracts_text_from_valid_pdf() -> None:
    document = pymupdf.open()
    page = document.new_page()
    page.insert_text((72, 72), "Jane Doe\nBackend Engineer")
    pdf_bytes = document.tobytes()
    document.close()

    response = client.post(
        "/api/resume/extract",
        files={"file": ("jane-resume.pdf", pdf_bytes, "application/pdf")},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["file_name"] == "jane-resume.pdf"
    assert body["page_count"] == 1
    assert "Jane Doe" in body["extracted_text"]
    assert "Backend Engineer" in body["extracted_text"]
    assert body["character_count"] == len(body["extracted_text"])


def test_extracts_skills_from_json_text() -> None:
    response = client.post(
        "/api/resume/skills",
        json={"text": "Java SpringBoot React.js Postgres Docker Git RESTful APIs NodeJS JS"},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["total_skills"] == len(body["skills"])
    assert {"Java", "JavaScript", "Spring Boot", "Node.js", "REST API", "React", "PostgreSQL", "Docker", "Git"} <= set(body["skills"])


def test_rejects_blank_skill_text() -> None:
    response = client.post("/api/resume/skills", json={"text": "  \n\t "})
    assert response.status_code == 400
    assert response.json()["detail"] == "Resume text must not be blank."


def test_rejects_blank_ats_text() -> None:
    response = client.post("/api/resume/ats-score", json={"text": "   "})
    assert response.status_code == 400
    assert response.json()["detail"] == "Resume text must not be blank."


def test_ats_score_returns_explainable_analysis() -> None:
    text = (
        "Software Developer. Email test@example.com. LinkedIn linkedin.com/in/test. GitHub github.com/test. "
        "Education B.Tech in Information Technology. Skills Java Spring Boot React PostgreSQL Docker Git REST API. "
        "Projects Employee Management System using Spring Boot React and PostgreSQL. "
        "Internship Software Developer Intern. Certifications Coursera Java Programming."
    )
    response = client.post("/api/resume/ats-score", json={"text": text})
    assert response.status_code == 200
    body = response.json()
    assert 0 <= body["ats_score"] <= 100
    assert body["grade"] in {"A+", "A", "B", "C", "D", "Needs Improvement"}
    assert {"Java", "Spring Boot", "React", "PostgreSQL", "Docker", "Git", "REST API"} <= set(body["detected_skills"])
    assert body["scoring_note"] == "This score is a heuristic resume-quality indicator and is not an official ATS vendor score."
    assert "Email address could not be detected" not in body["weaknesses"]
    assert "LinkedIn profile is not included" not in body["weaknesses"]


def test_extract_and_skills_works_with_generated_pdf() -> None:
    pdf_bytes = _pdf_with_text("Skills Java Spring Boot PostgreSQL")
    response = client.post(
        "/api/resume/extract-and-skills",
        files={"file": ("resume.pdf", pdf_bytes, "application/pdf")},
    )
    assert response.status_code == 200
    assert {"Java", "Spring Boot", "PostgreSQL"} <= set(response.json()["skills"])


def test_analyze_basic_works_with_generated_pdf() -> None:
    pdf_bytes = _pdf_with_text(
        "Summary Skills Java Spring Boot React PostgreSQL Docker. Education B.Tech Example University.\n"
        "Projects Inventory API using Spring Boot PostgreSQL. Internship Developer Intern.\n"
        "Email jane@example.com LinkedIn linkedin.com/in/jane GitHub github.com/jane Certifications Coursera."
    )
    response = client.post(
        "/api/resume/analyze-basic",
        files={"file": ("resume.pdf", pdf_bytes, "application/pdf")},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["file_name"] == "resume.pdf"
    assert body["page_count"] == 1
    assert body["character_count"] > 0
    assert 0 <= body["ats_score"] <= 100
    assert "Spring Boot" in body["detected_skills"]
    assert body["scoring_note"].startswith("This score is a heuristic")


def test_job_match_endpoint_returns_expected_comparison() -> None:
    response = client.post(
        "/api/resume/job-match",
        json={
            "resume_text": "Java Spring Boot React PostgreSQL Docker Git REST API",
            "job_description": "Java Spring Boot PostgreSQL REST API Docker AWS Kubernetes",
        },
    )
    assert response.status_code == 200
    body = response.json()
    assert body["match_percentage"] == 71
    assert body["resume_skill_count"] == 7
    assert body["job_skill_count"] == 7
    assert set(body["missing_skills"]) == {"AWS", "Kubernetes"}
    assert "cloud_devops" in body["category_matches"]


def test_job_match_rejects_blank_fields() -> None:
    blank_resume = client.post(
        "/api/resume/job-match", json={"resume_text": "  ", "job_description": "Java"}
    )
    blank_job = client.post(
        "/api/resume/job-match", json={"resume_text": "Java", "job_description": "\n "}
    )
    assert blank_resume.status_code == 400
    assert blank_resume.json()["detail"] == "Resume text must not be blank."
    assert blank_job.status_code == 400
    assert blank_job.json()["detail"] == "Job description must not be blank."


def test_analyze_job_match_uses_existing_pdf_extraction() -> None:
    response = client.post(
        "/api/resume/analyze-job-match",
        files={"file": ("resume.pdf", _pdf_with_text("Java Spring Boot PostgreSQL"), "application/pdf")},
        data={"job_description": "Java Spring Boot PostgreSQL Docker"},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["file_name"] == "resume.pdf"
    assert body["page_count"] == 1
    assert body["match_percentage"] == 75
    assert body["resume_skills"] == ["Java", "Spring Boot", "PostgreSQL"]
    assert body["job_skills"] == ["Java", "Spring Boot", "PostgreSQL", "Docker"]
    assert body["missing_skills"] == ["Docker"]


def _pdf_with_text(text: str) -> bytes:
    document = pymupdf.open()
    page = document.new_page()
    page.insert_textbox((50, 50, 550, 780), text, fontsize=10)
    pdf_bytes = document.tobytes()
    document.close()
    return pdf_bytes
