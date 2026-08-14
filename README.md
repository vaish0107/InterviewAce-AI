# InterviewAce AI

## Interview replay and coaching privacy

Completed interviews can be replayed from saved questions, answers, and existing evaluations. Replay never regenerates evaluation feedback automatically.

Coaching generation sends the AI service only the interview type, difficulty, question text, saved answer text, category, skill, adaptive-question flag, completed rubric scores, recorded weaknesses, and recorded missing key points. It does not send credentials, email, account data, database IDs, raw audio, raw resumes, or unrelated profile data. Coaching is practice guidance only and does not predict hiring outcomes, rank candidates, or infer intelligence or personality.

## Project Overview
InterviewAce AI is an AI-powered interview preparation platform designed to help job seekers improve their resumes, practice interviews, and build confidence before real-world interviews.

## Problem Statement
Many candidates struggle to stand out in competitive hiring processes because they receive limited feedback on their resumes and insufficient personalized interview practice. Traditional preparation methods are often generic and do not adapt to a candidate's background, target role, or interview style.

## Objectives
- Help users improve resume quality and ATS compatibility.
- Deliver personalized interview questions based on the user's target role.
- Provide actionable feedback and scoring for resume and interview performance.
- Support continuous progress tracking across multiple interview sessions.

## Main Workflow
1. User registers and logs in.
2. User uploads a resume.
3. The system extracts resume text and identifies skills.
4. AI provides resume feedback and an ATS-style score.
5. User chooses a target job role and interview type.
6. AI generates personalized interview questions.
7. User answers through text or browser-powered voice transcription.
8. AI evaluates each answer.
9. The platform generates feedback, scores, and improved answers.
10. Users track deterministic performance analytics over multiple interview sessions.

## Interview Analytics

Interview analytics are calculated by the Spring Boot backend from persisted, completed answer evaluations. They never call the AI service and never assign zero scores to unanswered or unevaluated questions.

```text
Interview evaluations
        ↓
Deterministic aggregation
        ↓
Interview Summary
        ↓
Progress Analytics
        ↓
Dashboard
```

Authenticated endpoints:

- `GET /api/interviews/{id}/summary` — owner-only per-interview completion and score summary.
- `GET /api/interviews/progress` — current user's category, skill, rubric, and chronological trend analytics.
- `GET /api/dashboard/summary` — current user's persisted resume, ATS, job-match, and interview counts/latest values.

Score averages use only evaluations whose status is `COMPLETED`. Category and skill results use the category and optional skill stored on each evaluated question. Strongest and lowest measured categories are returned only when at least two categories have evaluated data.

## Voice Interview Mode

Voice mode is available at `/interviews/{id}/voice` alongside the existing text interview route. It uses browser speech capabilities and the same persisted interview session:

```text
Browser microphone
        ↓
Browser speech recognition
        ↓
User-reviewed, editable transcript
        ↓
Existing Spring Boot answer endpoint
        ↓
Existing Gemini transcript evaluation
```

Questions can be read aloud with browser text-to-speech, including an optional saved auto-read preference. Microphone permission is requested before the first recording. The transcript is never saved automatically: the user can correct recognition errors before submitting it through the existing answer endpoint. InterviewAce does not upload or store raw microphone audio.

Speech recognition availability depends on browser support and may depend on the browser vendor's speech service. Unsupported browsers offer a direct switch back to the text interview.

## Adaptive Follow-up Interviews

After saving either a text answer or a voice transcript, the candidate can explicitly request an AI follow-up:

```text
Deterministic base question → saved answer → Gemini follow-up decision
→ optional persisted follow-up → existing answer evaluation → next base question
```

Follow-ups are grounded only in the current question and answer plus at most three recent answered questions. InterviewAce does not fabricate experience, and allows at most two adaptive follow-ups per base question. Adaptive generation is optional: a provider failure never prevents answering, navigating, evaluating existing answers, or completing the interview.

## Planned Features
- Resume upload and parsing
- Skills extraction and analysis
- ATS-style resume scoring
- Personalized interview question generation
- Answer evaluation and improvement suggestions
- Progress tracking and session history
- Browser-based voice interview support

## Technology Stack
### Frontend
- React
- TypeScript
- Vite
- Tailwind CSS
- React Router
- Axios

### Backend
- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL

### AI Service
- Python
- FastAPI
- Resume text extraction
- LLM integration
- Browser speech recognition (frontend only; no raw audio storage)

## High-Level Architecture
InterviewAce AI will follow a modular architecture with three main services:
- A React frontend for user experience and interview practice flows
- A Spring Boot backend for authentication, user management, and persistence
- A FastAPI AI service for resume analysis and interview generation/evaluation

## MVP Scope
- User registration and authentication
- Resume upload and parsing
- Resume feedback and ATS-style score
- Interview question generation
- Answer evaluation and feedback
- Interview summaries, dashboard statistics, and progress tracking

## Development Phases
1. Planning and architecture definition
2. Backend foundation and database design
3. Frontend experience and user flows
4. AI service integration
5. Testing, refinement, and deployment preparation

## Current Status
Interview summary and deterministic progress analytics implemented
