import axios from 'axios'

interface ApiErrorBody { message?: string; fieldErrors?: Record<string, string> }

export function getApiErrorMessage(error: unknown): string {
  if (!axios.isAxiosError<ApiErrorBody>(error)) return 'Something went wrong. Please try again.'
  if (!error.response) return 'Unable to reach InterviewAce. Check your connection and try again.'
  const backendMessage = error.response.data?.message
  if (backendMessage) return backendMessage
  const messages: Record<number, string> = {
    400: 'Please review the information you entered.',
    401: 'Your email or password is incorrect.',
    403: 'You do not have permission to perform this action.',
    404: 'The requested resource could not be found.',
    409: 'An account with these details already exists.',
    413: 'The selected file exceeds the 5 MB upload limit.',
    500: 'The server encountered an error. Please try again later.',
    502: 'The AI service returned an invalid response. Please try again.',
    503: 'The AI analysis service is temporarily unavailable. Please try again later.',
  }
  return messages[error.response.status] || 'The request could not be completed.'
}
