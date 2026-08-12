/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authService } from '../services/authService'
import { TOKEN_KEY } from '../services/api'
import type { LoginRequest, RegisterRequest, UserProfile, UserRole } from '../types/auth'

interface AuthContextValue {
  user: UserProfile | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (data: LoginRequest) => Promise<void>
  register: (data: RegisterRequest) => Promise<void>
  logout: () => void
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const logout = useCallback(() => { authService.logout(); setUser(null) }, [])
  const refreshUser = useCallback(async () => { setUser(await authService.getCurrentUser()) }, [])

  useEffect(() => {
    const restore = async () => {
      if (!localStorage.getItem(TOKEN_KEY)) { setIsLoading(false); return }
      try { await refreshUser() } catch { logout() } finally { setIsLoading(false) }
    }
    void restore()
    window.addEventListener('auth:unauthorized', logout)
    return () => window.removeEventListener('auth:unauthorized', logout)
  }, [logout, refreshUser])

  const login = useCallback(async (data: LoginRequest) => { await authService.login(data); await refreshUser() }, [refreshUser])
  const register = useCallback(async (data: RegisterRequest) => { await authService.register(data); await refreshUser() }, [refreshUser])
  const value = useMemo(() => ({ user, isAuthenticated: Boolean(user), isLoading, login, register, logout, refreshUser }), [user, isLoading, login, register, logout, refreshUser])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}

export function hasRole(userRole: UserRole, allowedRole?: UserRole) {
  return !allowedRole || userRole === allowedRole
}
