export interface JobMatchRequest { jobDescription: string }

export interface JobMatchCategory {
  requiredSkills: string[]
  matchedSkills: string[]
  missingSkills: string[]
  matchPercentage: number
}

export interface JobMatchAnalysis {
  id: number
  resumeId: number
  matchPercentage: number
  resumeSkillCount: number
  jobSkillCount: number
  matchedSkills: string[]
  missingSkills: string[]
  additionalResumeSkills: string[]
  categoryMatches: Record<string, JobMatchCategory>
  strengths: string[]
  recommendations: string[]
  matchingNote: string
  createdAt: string
}
