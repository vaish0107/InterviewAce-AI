import { api } from './api'
import type { DashboardSummary, InterviewProgress, InterviewSummary } from '../types/analytics'

export const analyticsService = {
  async getInterviewSummary(interviewId: number) { return (await api.get<InterviewSummary>(`/interviews/${interviewId}/summary`)).data },
  async getInterviewProgress() { return (await api.get<InterviewProgress>('/interviews/progress')).data },
  async getDashboardSummary() { return (await api.get<DashboardSummary>('/dashboard/summary')).data },
}
