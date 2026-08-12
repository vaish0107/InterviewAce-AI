export type UserRole = 'USER' | 'ADMIN'
export type AccountStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED'

export interface AuthResponse {
  token: string
  tokenType: string
  userId: number
  fullName: string
  email: string
  role: UserRole
}

export interface UserProfile {
  id: number
  fullName: string
  email: string
  role: UserRole
  accountStatus: AccountStatus
  createdAt: string
  updatedAt: string
}

export interface RegisterRequest { fullName: string; email: string; password: string }
export interface LoginRequest { email: string; password: string }
