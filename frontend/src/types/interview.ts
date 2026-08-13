export type InterviewType = 'TECHNICAL' | 'HR' | 'MIXED'
export type InterviewDifficulty = 'EASY' | 'MEDIUM' | 'HARD'
export type InterviewStatus = 'CREATED' | 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED'

export interface InterviewQuestion {
  id: number
  externalQuestionId: string
  questionText: string
  category: string
  skill: string | null
  difficulty: InterviewDifficulty
  questionOrder: number
  answerText: string | null
  answeredAt: string | null
  adaptive: boolean
  parentQuestionId: number | null
  followUpDepth: number
  focusArea: string | null
}

export interface InterviewSession {
  id: number
  resumeId: number | null
  resumeFileName: string | null
  interviewType: InterviewType
  difficulty: InterviewDifficulty
  totalQuestions: number
  detectedSkills: string[]
  status: InterviewStatus
  answeredQuestions: number
  startedAt: string | null
  completedAt: string | null
  createdAt: string
  updatedAt: string
  questions: InterviewQuestion[]
}

export interface CreateInterviewRequest {
  resumeId: number | null
  interviewType: InterviewType
  difficulty: InterviewDifficulty
  questionCount: number
}

export interface SubmitAnswerRequest { answer: string }
export interface FollowUpGeneration { created: boolean; question: InterviewQuestion | null; reason: string }

export type EvaluationStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
export interface InterviewAnswerEvaluation {
  id: number
  questionId: number
  overallScore: number | null
  relevanceScore: number | null
  correctnessScore: number | null
  completenessScore: number | null
  communicationScore: number | null
  strengths: string[]
  weaknesses: string[]
  missingKeyPoints: string[]
  improvedAnswer: string | null
  summary: string | null
  evaluationNote: string | null
  status: EvaluationStatus
  evaluatedAt: string | null
  failureMessage: string | null
}
