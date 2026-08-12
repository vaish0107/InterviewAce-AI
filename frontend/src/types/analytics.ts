export interface InterviewTrendPoint { interviewId: number; completedAt: string; averageScore: number }

export interface InterviewSummary {
  interviewId: number; interviewType: string; difficulty: string; status: string
  totalQuestions: number; answeredQuestions: number; evaluatedQuestions: number; unansweredQuestions: number
  averageScore: number | null; averageRelevance: number | null; averageCorrectness: number | null
  averageCompleteness: number | null; averageCommunication: number | null
  categoryScores: Record<string, number>; skillScores: Record<string, number>
  strongestCategory: string | null; weakestCategory: string | null
  startedAt: string | null; completedAt: string | null
}

export interface InterviewProgress {
  totalInterviews: number; completedInterviews: number; inProgressInterviews: number
  totalQuestionsAnswered: number; totalEvaluatedAnswers: number; overallAverageScore: number | null
  averageRelevance: number | null; averageCorrectness: number | null
  averageCompleteness: number | null; averageCommunication: number | null
  categoryAverages: Record<string, number>; skillAverages: Record<string, number>
  scoreTrend: InterviewTrendPoint[]; strongestCategory: string | null; weakestCategory: string | null
}

export interface DashboardSummary {
  resumeCount: number; completedResumeAnalyses: number; jobMatchCount: number; interviewCount: number
  completedInterviewCount: number; evaluatedAnswerCount: number; latestAtsScore: number | null
  latestJobMatchPercentage: number | null; interviewAverageScore: number | null
}
