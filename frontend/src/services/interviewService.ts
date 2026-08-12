import { api } from './api'
import type { CreateInterviewRequest, InterviewAnswerEvaluation, InterviewSession, SubmitAnswerRequest } from '../types/interview'

export const interviewService = {
  async createInterview(request: CreateInterviewRequest) { return (await api.post<InterviewSession>('/interviews', request)).data },
  async getInterviews() { return (await api.get<InterviewSession[]>('/interviews')).data },
  async getInterview(id: number) { return (await api.get<InterviewSession>(`/interviews/${id}`)).data },
  async submitAnswer(sessionId: number, questionId: number, answer: string) {
    const body: SubmitAnswerRequest = { answer }
    return (await api.put<InterviewSession>(`/interviews/${sessionId}/questions/${questionId}/answer`, body)).data
  },
  async completeInterview(id: number) { return (await api.post<InterviewSession>(`/interviews/${id}/complete`)).data },
  async evaluateAnswer(sessionId: number, questionId: number) {
    return (await api.post<InterviewAnswerEvaluation>(`/interviews/${sessionId}/questions/${questionId}/evaluate`)).data
  },
  async getAnswerEvaluation(sessionId: number, questionId: number) {
    return (await api.get<InterviewAnswerEvaluation>(`/interviews/${sessionId}/questions/${questionId}/evaluation`)).data
  },
}
