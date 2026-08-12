import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth, hasRole } from '../../context/AuthContext'
import type { UserRole } from '../../types/auth'
import { LoadingSpinner } from '../common/LoadingSpinner'

export function ProtectedRoute({ role }: { role?: UserRole }) {
  const { user, isAuthenticated, isLoading } = useAuth()
  const location = useLocation()
  if (isLoading) return <LoadingSpinner label="Restoring your session…" />
  if (!isAuthenticated) return <Navigate to="/login" replace state={{ from: location }} />
  if (user && !hasRole(user.role, role)) return <Navigate to="/dashboard" replace />
  return <Outlet />
}
