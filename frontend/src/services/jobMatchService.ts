import { api } from './api'
import type { JobMatchAnalysis, JobMatchRequest } from '../types/jobMatch'

export const jobMatchService = {
  async analyzeJobMatch(resumeId: number, jobDescription: string) {
    const body: JobMatchRequest = { jobDescription }
    return (await api.post<JobMatchAnalysis>(`/resumes/${resumeId}/job-match`, body)).data
  },
  async getLatestJobMatch(resumeId: number) {
    return (await api.get<JobMatchAnalysis>(`/resumes/${resumeId}/job-match/latest`)).data
  },
  async getJobMatchHistory(resumeId: number) {
    return (await api.get<JobMatchAnalysis[]>(`/resumes/${resumeId}/job-matches`)).data
  },
}
