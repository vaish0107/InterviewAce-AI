# InterviewAce AI Database Design

## Purpose
This document defines the PostgreSQL schema for the MVP of InterviewAce AI. It covers the core entities needed for authentication, resume processing, interview practice, evaluation, and reporting.

## Design Principles
- Use PostgreSQL-friendly data types such as `BIGINT`, `VARCHAR`, `TEXT`, `BOOLEAN`, `NUMERIC`, and `TIMESTAMP`.
- Favor explicit constraints and indexes for reliability and query performance.
- Keep AI-generated content advisory and separate from deterministic user identity data.
- Support future extension for voice-based interviews without changing the core relational model too aggressively.

## Recommended PostgreSQL Enums

```sql
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'INACTIVE', 'LOCKED');
CREATE TYPE upload_status AS ENUM ('UPLOADED', 'PROCESSING', 'COMPLETED', 'FAILED');
CREATE TYPE analysis_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');
CREATE TYPE interview_type AS ENUM ('HR', 'TECHNICAL', 'BEHAVIORAL', 'RESUME_BASED', 'MIXED');
CREATE TYPE difficulty_level AS ENUM ('EASY', 'MEDIUM', 'HARD');
CREATE TYPE session_status AS ENUM ('CREATED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'FAILED');
CREATE TYPE answer_source AS ENUM ('TEXT', 'VOICE');
CREATE TYPE skill_source AS ENUM ('RESUME', 'USER_ENTERED', 'INTERVIEW_ANALYSIS');
```

## Core Tables

### users
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    account_status account_status NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### resumes
```sql
CREATE TABLE resumes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    extracted_text TEXT,
    upload_status upload_status NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### resume_analyses
```sql
CREATE TABLE resume_analyses (
    id BIGSERIAL PRIMARY KEY,
    resume_id BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    ats_score INTEGER,
    overall_summary TEXT,
    strengths TEXT,
    weaknesses TEXT,
    suggestions TEXT,
    detected_skills TEXT,
    missing_keywords TEXT,
    analyzed_at TIMESTAMP,
    analysis_status analysis_status NOT NULL
);
```

### job_roles
```sql
CREATE TABLE job_roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    category VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### interview_sessions
```sql
CREATE TABLE interview_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    resume_id BIGINT REFERENCES resumes(id) ON DELETE SET NULL,
    job_role_id BIGINT NOT NULL REFERENCES job_roles(id) ON DELETE RESTRICT,
    interview_type interview_type NOT NULL,
    difficulty difficulty_level NOT NULL,
    question_count INTEGER NOT NULL,
    session_status session_status NOT NULL,
    overall_score NUMERIC(5,2),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### interview_questions
```sql
CREATE TABLE interview_questions (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES interview_sessions(id) ON DELETE CASCADE,
    question_order INTEGER NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(30),
    difficulty difficulty_level,
    expected_topics TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### interview_answers
```sql
CREATE TABLE interview_answers (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT UNIQUE NOT NULL REFERENCES interview_questions(id) ON DELETE CASCADE,
    answer_text TEXT,
    answer_source answer_source,
    relevance_score NUMERIC(5,2),
    accuracy_score NUMERIC(5,2),
    communication_score NUMERIC(5,2),
    completeness_score NUMERIC(5,2),
    overall_score NUMERIC(5,2),
    submitted_at TIMESTAMP,
    evaluated_at TIMESTAMP
);
```

### answer_feedback
```sql
CREATE TABLE answer_feedback (
    id BIGSERIAL PRIMARY KEY,
    answer_id BIGINT UNIQUE NOT NULL REFERENCES interview_answers(id) ON DELETE CASCADE,
    strengths TEXT,
    weaknesses TEXT,
    improvement_suggestions TEXT,
    improved_answer TEXT,
    evaluator_summary TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### interview_reports
```sql
CREATE TABLE interview_reports (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT UNIQUE NOT NULL REFERENCES interview_sessions(id) ON DELETE CASCADE,
    final_score NUMERIC(5,2),
    performance_summary TEXT,
    strong_areas TEXT,
    weak_areas TEXT,
    recommended_topics TEXT,
    learning_roadmap TEXT,
    report_file_path VARCHAR(500),
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### user_skills
```sql
CREATE TABLE user_skills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    proficiency_level VARCHAR(30),
    source skill_source,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Relationships and Delete Behavior
- One user has many resumes.
- One resume has many analyses over time.
- One user has many interview sessions.
- One job role has many interview sessions.
- One interview session has many questions.
- One question has zero or one answer.
- One answer has zero or one feedback record.
- One session has zero or one final report.
- One user has many skills.

### Recommended cascade behavior
- Delete a user -> delete their resumes, interview sessions, and skills.
- Delete a resume -> delete its analyses; keep interview sessions referencing it by setting `resume_id` to `NULL` where appropriate.
- Delete a session -> delete its questions, answer records, feedback, and report.
- Delete a job role -> prevent deletion if sessions still reference it; this protects historical reporting integrity.
- Delete an answer -> delete its feedback record.

## Indexes and Constraints

### Recommended indexes
```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_resumes_user_id ON resumes(user_id);
CREATE INDEX idx_resume_analyses_resume_id ON resume_analyses(resume_id);
CREATE INDEX idx_interview_sessions_user_id ON interview_sessions(user_id);
CREATE INDEX idx_interview_sessions_job_role_id ON interview_sessions(job_role_id);
CREATE INDEX idx_interview_sessions_status_created_at ON interview_sessions(session_status, created_at);
CREATE INDEX idx_interview_questions_session_id ON interview_questions(session_id);
CREATE INDEX idx_interview_answers_question_id ON interview_answers(question_id);
CREATE INDEX idx_user_skills_user_id ON user_skills(user_id);
```

### Additional constraints
```sql
ALTER TABLE resume_analyses
ADD CONSTRAINT chk_resume_analyses_ats_score
CHECK (ats_score IS NULL OR (ats_score >= 0 AND ats_score <= 100));

ALTER TABLE interview_answers
ADD CONSTRAINT chk_answer_scores
CHECK (
    (relevance_score IS NULL OR (relevance_score >= 0 AND relevance_score <= 100)) AND
    (accuracy_score IS NULL OR (accuracy_score >= 0 AND accuracy_score <= 100)) AND
    (communication_score IS NULL OR (communication_score >= 0 AND communication_score <= 100)) AND
    (completeness_score IS NULL OR (completeness_score >= 0 AND completeness_score <= 100)) AND
    (overall_score IS NULL OR (overall_score >= 0 AND overall_score <= 100))
);

ALTER TABLE interview_sessions
ADD CONSTRAINT chk_question_count_positive
CHECK (question_count > 0);

ALTER TABLE interview_questions
ADD CONSTRAINT uq_session_question_order UNIQUE (session_id, question_order);
```

## MVP Decisions
- One answer per generated question is enforced with a unique `question_id` on `interview_answers`.
- Resume files are initially limited to PDF only for MVP simplicity.
- Text interviews are the MVP experience; voice-based interviews are future scope.
- Skills are stored as normalized strings initially to keep data handling simple and consistent.
- AI-generated output is advisory and may contain errors; it should never be treated as deterministic truth.
- Report PDF generation can be added after the text-based report flow is working.

## Notes for Implementation
- Store resume files outside the database in a secure object store or file system path, and keep only metadata in PostgreSQL.
- Use password hashing with a strong algorithm such as Argon2id or bcrypt; never store plaintext passwords.
- Keep AI service access scoped to the minimum data needed for resume parsing, question generation, and evaluation.
