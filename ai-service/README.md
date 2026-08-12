# InterviewAce AI Service

This FastAPI service provides health reporting, in-memory PDF resume text extraction, interview question generation, and structured AI answer evaluation. It does not persist uploaded files or evaluations; the Spring Boot backend owns persistence.

## Windows setup

```powershell
cd ai-service
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
$env:GEMINI_API_KEY="your-key"
# Optional; defaults to gemini-2.5-flash
$env:GEMINI_MODEL="gemini-2.5-flash"
uvicorn app.main:app --reload --port 8000
```

Run tests with:

```powershell
python -m pytest
```

After startup, health is available at `http://localhost:8000/health` and Swagger UI at `http://localhost:8000/docs`.

`POST /api/interview/evaluate-answer` uses Gemini structured output and accepts only the question, saved answer, category, skill, and difficulty. It returns rubric scores and structured feedback. The endpoint does not receive resume text, profile data, or authentication tokens. Copy `ai-service/.env.example` to `ai-service/.env` for local configuration; never commit a real API key.

Keep this service bound to an internal interface in production. The extraction endpoint intentionally has no authentication because it is designed for future server-to-server calls from the Spring Boot backend.
