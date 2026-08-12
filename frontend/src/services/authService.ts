import { api, TOKEN_KEY, USER_KEY } from './api'
import type { AuthResponse, LoginRequest, RegisterRequest, UserProfile } from '../types/auth'

function saveSession(response: AuthResponse) {
  localStorage.setItem(TOKEN_KEY, response.token)
  localStorage.setItem(USER_KEY, JSON.stringify({
    id: response.userId,
    fullName: response.fullName,
    email: response.email,
    role: response.role,
  }))
}

export const authService = {
  async register(data: RegisterRequest) {
    const response = await api.post<AuthResponse>('/auth/register', data)
    saveSession(response.data)
    return response.data
  },
  async login(data: LoginRequest) {
    const response = await api.post<AuthResponse>('/auth/login', data)
    saveSession(response.data)
    return response.data
  },
  async getCurrentUser() {
    const response = await api.get<UserProfile>('/users/me')
    localStorage.setItem(USER_KEY, JSON.stringify(response.data))
    return response.data
  },
  logout() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  },
}
