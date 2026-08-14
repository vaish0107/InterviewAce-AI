import { api } from './api'
import axios from 'axios'
import type { InterviewCoachingReport } from '../types/coaching'
export const coachingService = {
  async generateCoaching(interviewId: number) { return (await api.post<InterviewCoachingReport>(`/interviews/${interviewId}/coaching`)).data },
  async getCoaching(interviewId: number): Promise<InterviewCoachingReport | null> {
    try { return (await api.get<InterviewCoachingReport>(`/interviews/${interviewId}/coaching`)).data }
    catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 404) return null
      throw error
    }
  },
}
