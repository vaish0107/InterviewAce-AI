import { api } from './api'
import type { Resume, ResumeAnalysis, ResumeUploadResponse } from '../types/resume'

export const resumeService = {
  async uploadResume(file: File, onProgress?: (percentage: number) => void) {
    const body = new FormData()
    body.append('file', file)
    const response = await api.post<ResumeUploadResponse>('/resumes', body, {
      onUploadProgress: event => {
        if (event.total && onProgress) onProgress(Math.round((event.loaded * 100) / event.total))
      },
    })
    return response.data
  },
  async getResumes() { return (await api.get<Resume[]>('/resumes')).data },
  async getResume(id: number) { return (await api.get<Resume>(`/resumes/${id}`)).data },
  async deleteResume(id: number) { await api.delete(`/resumes/${id}`) },
  async analyzeResume(id: number) { return (await api.post<ResumeAnalysis>(`/resumes/${id}/analyze`)).data },
  async getResumeAnalysis(id: number) { return (await api.get<ResumeAnalysis>(`/resumes/${id}/analysis`)).data },
}
