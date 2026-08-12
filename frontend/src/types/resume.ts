export type ResumeStatus = 'UPLOADED' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
export type AnalysisStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'

export interface Resume {
  id: number
  originalFileName: string
  fileType: string
  fileSize: number
  uploadStatus: ResumeStatus
  uploadedAt: string
  updatedAt: string
}

export interface ResumeUploadResponse {
  id: number
  originalFileName: string
  fileSize: number
  uploadStatus: ResumeStatus
  message: string
  uploadedAt: string
}

export interface ResumeAnalysis {
  id: number
  resumeId: number
  atsScore: number | null
  grade: string | null
  detectedSkills: string[]
  strengths: string[]
  weaknesses: string[]
  recommendations: string[]
  status: AnalysisStatus
  scoringNote: string | null
  analyzedAt: string | null
  failureMessage: string | null
}
