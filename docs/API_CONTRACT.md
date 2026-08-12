# InterviewAce AI MVP REST API Contract

## Overview
This contract defines the MVP REST API for the InterviewAce AI backend. It focuses on authentication, resume handling, interview creation, answer evaluation, reporting, and dashboard summaries.

## Base URL
- `/api`

## Authentication
All protected routes require a bearer token in the `Authorization` header.

Example:
```http
Authorization: Bearer <jwt-token>
```

## Authentication Endpoints

### POST /api/auth/register
Purpose: Create a new user account.

Authentication: None.

Request body:
```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "password": "StrongPassword123!"
}
```

Response body:
```json
{
  "user": {
    "id": 1,
    "fullName": "Jane Doe",
    "email": "jane@example.com",
    "role": "USER",
    "accountStatus": "ACTIVE"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Possible status codes:
- `201 Created`
- `400 Bad Request`
- `409 Conflict`

Ownership/security rules:
- The email must be unique.
- Password hashes must never be returned in the response.

### POST /api/auth/login
Purpose: Authenticate a user and issue a token.

Authentication: None.

Request body:
```json
{
  "email": "jane@example.com",
  "password": "StrongPassword123!"
}
```

Response body:
```json
{
  "user": {
    "id": 1,
    "fullName": "Jane Doe",
    "email": "jane@example.com",
    "role": "USER",
    "accountStatus": "ACTIVE"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`
- `400 Bad Request`

Ownership/security rules:
- Passwords are never returned.
- Admin and user roles are determined by the persisted user record.

### GET /api/users/me
Purpose: Retrieve the authenticated user profile.

Authentication: Required.

Response body:
```json
{
  "id": 1,
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "role": "USER",
  "accountStatus": "ACTIVE",
  "createdAt": "2026-08-04T10:15:30Z"
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`

Ownership/security rules:
- Only the authenticated user can access this profile.

## Resume Endpoints

### POST /api/resumes
Purpose: Upload a resume file and create a resume metadata record.

Authentication: Required.

Request body:
```json
{
  "fileName": "jane_resume.pdf",
  "fileType": "application/pdf",
  "fileSize": 245760
}
```

Response body:
```json
{
  "id": 10,
  "userId": 1,
  "originalFileName": "jane_resume.pdf",
  "storedFileName": "resume_10_20260804.pdf",
  "filePath": "/uploads/resumes/resume_10_20260804.pdf",
  "fileType": "application/pdf",
  "fileSize": 245760,
  "uploadStatus": "UPLOADED",
  "uploadedAt": "2026-08-04T10:20:00Z",
  "updatedAt": "2026-08-04T10:20:00Z"
}
```

Possible status codes:
- `201 Created`
- `400 Bad Request`
- `401 Unauthorized`
- `413 Payload Too Large`

Ownership/security rules:
- The uploaded resume must belong to the authenticated user.
- File paths must not be exposed as public URLs.
- File validation must reject unsupported types and suspicious content.

### GET /api/resumes
Purpose: List resumes owned by the authenticated user.

Authentication: Required.

Response body:
```json
[
  {
    "id": 10,
    "originalFileName": "jane_resume.pdf",
    "uploadStatus": "COMPLETED",
    "uploadedAt": "2026-08-04T10:20:00Z"
  }
]
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`

Ownership/security rules:
- Only resumes belonging to the user are returned.

### GET /api/resumes/{id}
Purpose: Retrieve details for a specific resume.

Authentication: Required.

Response body:
```json
{
  "id": 10,
  "userId": 1,
  "originalFileName": "jane_resume.pdf",
  "storedFileName": "resume_10_20260804.pdf",
  "fileType": "application/pdf",
  "fileSize": 245760,
  "uploadStatus": "COMPLETED",
  "extractedText": "Experienced software engineer...",
  "uploadedAt": "2026-08-04T10:20:00Z",
  "updatedAt": "2026-08-04T10:20:00Z"
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- Users may view only their own resumes.

### DELETE /api/resumes/{id}
Purpose: Delete a resume and its related analysis records.

Authentication: Required.

Possible status codes:
- `204 No Content`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- The user can delete only their own resumes.

### POST /api/resumes/{id}/analyze
Purpose: Analyze an owned PDF through FastAPI and persist the completed result.

Authentication: Required.

Request body: None.

Response body:
```json
{
  "id": 20,
  "resumeId": 10,
  "atsScore": 82,
  "grade": "A",
  "detectedSkills": ["Java", "Spring", "PostgreSQL"],
  "strengths": ["Strong technical skill coverage"],
  "weaknesses": ["LinkedIn profile is not included"],
  "recommendations": ["Add a professional LinkedIn profile link."],
  "status": "COMPLETED",
  "scoringNote": "This score is a heuristic resume-quality indicator and is not an official ATS vendor score.",
  "analyzedAt": "2026-08-04T10:25:00",
  "failureMessage": null
}
```

Possible status codes:
- `200 OK`
- `502 Bad Gateway`
- `503 Service Unavailable`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- Only the owner can trigger analysis for a resume.
- The AI service should receive only the minimum required data.
- Admins do not bypass ownership through this self-service route.

### GET /api/resumes/{id}/analysis
Purpose: Retrieve the latest resume analysis result.

Authentication: Required.

Response body:
```json
{
  "id": 20,
  "resumeId": 10,
  "atsScore": 82,
  "grade": "A",
  "detectedSkills": ["Java", "Spring", "PostgreSQL"],
  "strengths": ["Strong Java and distributed systems experience"],
  "weaknesses": ["Limited cloud-native deployment examples"],
  "recommendations": ["Add Kubernetes deployment experience"],
  "status": "COMPLETED",
  "scoringNote": "This score is a heuristic resume-quality indicator and is not an official ATS vendor score.",
  "analyzedAt": "2026-08-04T10:25:00",
  "failureMessage": null
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- Only the owner may view analysis for their resume.

Analysis flow:

```text
Spring Boot
    ↓
FastAPI /api/resume/analyze-basic
    ↓
ATS-style analysis
    ↓
PostgreSQL resume_analyses persistence
```

### POST /api/resumes/{id}/job-match
Purpose: Extract text from an owned resume in FastAPI, match explicitly detected skills against a job description, and persist the result.

Authentication: Required.

Request body:
```json
{
  "jobDescription": "We are looking for a Java developer with Spring Boot, PostgreSQL, REST API, Docker, AWS and Kubernetes experience."
}
```

Response: `201 Created`
```json
{
  "id": 30,
  "resumeId": 10,
  "matchPercentage": 71,
  "resumeSkillCount": 8,
  "jobSkillCount": 7,
  "matchedSkills": ["Java", "Spring Boot", "REST API", "PostgreSQL", "Docker"],
  "missingSkills": ["AWS", "Kubernetes"],
  "additionalResumeSkills": ["Git"],
  "categoryMatches": {
    "backend": {
      "requiredSkills": ["Java", "Spring Boot", "REST API"],
      "matchedSkills": ["Java", "Spring Boot", "REST API"],
      "missingSkills": [],
      "matchPercentage": 100
    }
  },
  "strengths": ["Strong backend skill alignment"],
  "recommendations": ["Add evidence of AWS and Kubernetes experience if applicable"],
  "matchingNote": "This match is based on explicitly detected skills and does not measure actual proficiency or hiring suitability.",
  "createdAt": "2026-08-09T15:20:00"
}
```

### GET /api/resumes/{id}/job-matches
Purpose: Return all persisted job matches for an owned resume, newest first.

Authentication: Required. Response: `200 OK`.

### GET /api/resumes/{id}/job-match/latest
Purpose: Return the latest persisted job match for an owned resume.

Authentication: Required. Response: `200 OK`, or `404 Not Found` when no match exists.

Ownership/security rules:
- The authenticated user is always derived from JWT.
- Users and admins may access only resumes they own through these self-service endpoints.
- Resume text and filesystem paths are never accepted from the client or exposed in responses.

Job-match flow:

```text
Resume PDF
    ↓
Spring Boot
    ↓
FastAPI text extraction
    ↓
FastAPI deterministic job matching
    ↓
Spring Boot persistence
    ↓
PostgreSQL job_match_analyses
```

This match is based on explicitly detected skills and does not measure actual proficiency or hiring suitability.

## Job Role Endpoints

### GET /api/job-roles
Purpose: List active job roles.

Authentication: Required for regular users; admin-only management may be added later.

Response body:
```json
[
  {
    "id": 3,
    "roleName": "Backend Engineer",
    "description": "Build APIs and services",
    "category": "Software Engineering",
    "active": true
  }
]
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`

Ownership/security rules:
- Regular users can view active roles only.
- Admin can manage the role catalog.

### GET /api/job-roles/{id}
Purpose: Retrieve one job role.

Authentication: Required.

Response body:
```json
{
  "id": 3,
  "roleName": "Backend Engineer",
  "description": "Build APIs and services",
  "category": "Software Engineering",
  "active": true
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`
- `404 Not Found`

Ownership/security rules:
- Users may view public role metadata.
- Admin manages role definitions.

## Interview Session Endpoints

### POST /api/interviews
Purpose: Create a new interview session for a user and target role.

Authentication: Required.

Request body:
```json
{
  "resumeId": 10,
  "jobRoleId": 3,
  "interviewType": "TECHNICAL",
  "difficulty": "MEDIUM",
  "questionCount": 5
}
```

Response body:
```json
{
  "id": 77,
  "userId": 1,
  "resumeId": 10,
  "jobRoleId": 3,
  "interviewType": "TECHNICAL",
  "difficulty": "MEDIUM",
  "questionCount": 5,
  "sessionStatus": "CREATED",
  "createdAt": "2026-08-04T10:30:00Z"
}
```

Possible status codes:
- `201 Created`
- `400 Bad Request`
- `401 Unauthorized`
- `404 Not Found`

Ownership/security rules:
- The session must be created for the authenticated user.
- The job role must exist and be active.

### GET /api/interviews
Purpose: List interview sessions for the authenticated user.

Authentication: Required.

Response body:
```json
[
  {
    "id": 77,
    "jobRoleId": 3,
    "interviewType": "TECHNICAL",
    "sessionStatus": "IN_PROGRESS",
    "createdAt": "2026-08-04T10:30:00Z"
  }
]
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`

Ownership/security rules:
- Only the owner’s sessions are returned.

### GET /api/interviews/{id}
Purpose: Retrieve one interview session with its metadata.

Authentication: Required.

Response body:
```json
{
  "id": 77,
  "userId": 1,
  "resumeId": 10,
  "jobRoleId": 3,
  "interviewType": "TECHNICAL",
  "difficulty": "MEDIUM",
  "questionCount": 5,
  "sessionStatus": "IN_PROGRESS",
  "startedAt": "2026-08-04T10:31:00Z",
  "createdAt": "2026-08-04T10:30:00Z"
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- Users may access only sessions they own.

### POST /api/interviews/{id}/start
Purpose: Mark a created session as in progress and request initial questions from the AI service.

Authentication: Required.

Response body:
```json
{
  "sessionId": 77,
  "sessionStatus": "IN_PROGRESS",
  "message": "Interview session started"
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- Only the owner may start the session.

### POST /api/interviews/{id}/cancel
Purpose: Cancel an interview session.

Authentication: Required.

Response body:
```json
{
  "sessionId": 77,
  "sessionStatus": "CANCELLED"
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- Only the owner can cancel the session.

## Questions and Answers Endpoints

### GET /api/interviews/{id}/questions
Purpose: Retrieve generated questions for a session.

Authentication: Required.

Response body:
```json
[
  {
    "id": 101,
    "sessionId": 77,
    "questionOrder": 1,
    "questionText": "Describe a time you improved a backend service.",
    "questionType": "TECHNICAL",
    "difficulty": "MEDIUM",
    "expectedTopics": ["API design", "performance"]
  }
]
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- Users can view only their own session questions.

### POST /api/questions/{questionId}/answer
Purpose: Submit an answer to a generated question.

Authentication: Required.

Request body:
```json
{
  "answerText": "I improved a service by introducing caching and monitoring."
}
```

Response body:
```json
{
  "id": 201,
  "questionId": 101,
  "answerText": "I improved a service by introducing caching and monitoring.",
  "answerSource": "TEXT",
  "overallScore": 82.5,
  "submittedAt": "2026-08-04T10:35:00Z"
}
```

Possible status codes:
- `201 Created`
- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- One answer per question is allowed.
- The user can submit an answer only if they own the parent session.

### GET /api/questions/{questionId}/feedback
Purpose: Retrieve feedback for a submitted answer.

Authentication: Required.

Response body:
```json
{
  "id": 301,
  "answerId": 201,
  "strengths": "Clear structure and relevant examples",
  "weaknesses": "Could include more measurable impact",
  "improvementSuggestions": "Mention metrics and trade-offs",
  "improvedAnswer": "I improved a service by introducing caching...",
  "evaluatorSummary": "Good answer overall but needs stronger quantitative evidence.",
  "createdAt": "2026-08-04T10:36:00Z"
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- Users may view feedback only for answers on their own sessions.

## Report Endpoints

### POST /api/interviews/{id}/complete
Purpose: Finalize the interview session and generate a report.

Authentication: Required.

Response body:
```json
{
  "sessionId": 77,
  "sessionStatus": "COMPLETED",
  "reportId": 401,
  "message": "Interview completed and report generated"
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- Only the owner can complete a session.
- The report is tied to the session and should not be visible to other users.

### GET /api/interviews/{id}/report
Purpose: Retrieve the final interview report.

Authentication: Required.

Response body:
```json
{
  "id": 401,
  "sessionId": 77,
  "finalScore": 84.5,
  "performanceSummary": "Strong technical depth and structured communication.",
  "strongAreas": ["Problem solving", "clarity"],
  "weakAreas": ["Quantified impact"],
  "recommendedTopics": ["System design", "Trade-offs"],
  "learningRoadmap": ["Practice more system design", "Use metrics in examples"],
  "generatedAt": "2026-08-04T10:40:00Z"
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Ownership/security rules:
- Users may access only their own reports.

## Dashboard Endpoints

### GET /api/dashboard/summary
Purpose: Retrieve a user-level summary of activity and performance.

Authentication: Required.

Response body:
```json
{
  "resumeCount": 2,
  "interviewSessionCount": 5,
  "completedInterviewCount": 3,
  "averageScore": 81.2
}
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`

Ownership/security rules:
- The summary is scoped to the authenticated user only.

### GET /api/dashboard/performance-trend
Purpose: Retrieve historical performance trend data.

Authentication: Required.

Response body:
```json
[
  {
    "date": "2026-08-01",
    "averageScore": 76.5
  },
  {
    "date": "2026-08-02",
    "averageScore": 79.0
  }
]
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`

Ownership/security rules:
- The data must be limited to the authenticated user’s sessions.

### GET /api/dashboard/skill-performance
Purpose: Retrieve skill-based performance breakdown.

Authentication: Required.

Response body:
```json
[
  {
    "skill": "Java",
    "averageScore": 84.0,
    "sessionCount": 3
  }
]
```

Possible status codes:
- `200 OK`
- `401 Unauthorized`

Ownership/security rules:
- The data must be limited to the authenticated user’s history.

## Sample JSON

### Register request
```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "password": "StrongPassword123!"
}
```

### Register response
```json
{
  "user": {
    "id": 1,
    "fullName": "Jane Doe",
    "email": "jane@example.com",
    "role": "USER",
    "accountStatus": "ACTIVE"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Login request
```json
{
  "email": "jane@example.com",
  "password": "StrongPassword123!"
}
```

### Login response
```json
{
  "user": {
    "id": 1,
    "fullName": "Jane Doe",
    "email": "jane@example.com",
    "role": "USER",
    "accountStatus": "ACTIVE"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Resume upload metadata response
```json
{
  "id": 10,
  "userId": 1,
  "originalFileName": "jane_resume.pdf",
  "storedFileName": "resume_10_20260804.pdf",
  "filePath": "/uploads/resumes/resume_10_20260804.pdf",
  "fileType": "application/pdf",
  "fileSize": 245760,
  "uploadStatus": "UPLOADED"
}
```

### Resume analysis response
```json
{
  "resumeId": 10,
  "analysisStatus": "COMPLETED",
  "atsScore": 82,
  "overallSummary": "Strong backend engineering profile",
  "detectedSkills": ["Java", "Spring", "PostgreSQL"]
}
```

### Interview creation response
```json
{
  "id": 77,
  "userId": 1,
  "resumeId": 10,
  "jobRoleId": 3,
  "interviewType": "TECHNICAL",
  "difficulty": "MEDIUM",
  "questionCount": 5,
  "sessionStatus": "CREATED"
}
```

### Generated question response
```json
{
  "id": 101,
  "sessionId": 77,
  "questionOrder": 1,
  "questionText": "Describe a time you improved a backend service.",
  "questionType": "TECHNICAL",
  "difficulty": "MEDIUM"
}
```

### Answer submission response
```json
{
  "id": 201,
  "questionId": 101,
  "answerText": "I improved a service by introducing caching and monitoring.",
  "answerSource": "TEXT",
  "overallScore": 82.5,
  "submittedAt": "2026-08-04T10:35:00Z"
}
```

### Answer feedback response
```json
{
  "id": 301,
  "answerId": 201,
  "strengths": "Clear structure and relevant examples",
  "weaknesses": "Could include more measurable impact",
  "improvementSuggestions": "Mention metrics and trade-offs",
  "improvedAnswer": "I improved a service by introducing caching and monitoring, which reduced latency by 30%.",
  "evaluatorSummary": "Good answer overall but needs stronger quantitative evidence."
}
```

### Final interview report response
```json
{
  "id": 401,
  "sessionId": 77,
  "finalScore": 84.5,
  "performanceSummary": "Strong technical depth and structured responses.",
  "strongAreas": ["Problem solving", "clarity"],
  "weakAreas": ["Quantified impact"],
  "recommendedTopics": ["System design", "Trade-offs"],
  "learningRoadmap": ["Practice more system design", "Use metrics in examples"]
}
```

## Data Ownership and Security
- Users can access only their own resumes.
- Users can access only their own interview sessions.
- Users cannot access another user’s answers or reports.
- Admin can manage job roles and view platform-level statistics.
- Password hashes are never exposed.
- Resume file paths should not be directly public.
- Uploaded files require validation.
- The AI service should receive only the minimum necessary data.
