export type CoachingStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
export interface CoachingFocusArea { title: string; reason: string; priority: string; relatedSkills: string[] }
export interface PracticePlanItem { order: number; activity: string; focus: string; suggestedQuestionCount: number }
export interface InterviewCoachingReport { id: number; interviewId: number; summary: string | null; primaryFocusAreas: CoachingFocusArea[]; practiceRecommendations: string[]; revisionTopics: string[]; communicationTips: string[]; nextPracticePlan: PracticePlanItem[]; coachingNote: string | null; status: CoachingStatus; failureMessage: string | null; generatedAt: string | null; createdAt: string; updatedAt: string }
