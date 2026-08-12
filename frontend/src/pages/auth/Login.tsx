import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { FiEye, FiEyeOff } from 'react-icons/fi'
import { useAuth } from '../../context/AuthContext'
import { getApiErrorMessage } from '../../utils/getApiErrorMessage'
import { AuthShell } from '../../components/auth/AuthShell'
import { Input } from '../../components/common/Input'
import { Button } from '../../components/common/Button'
import { Alert } from '../../components/common/Alert'

const schema = z.object({ email: z.email('Enter a valid email address'), password: z.string().min(1, 'Password is required') })
type FormData = z.infer<typeof schema>

export function Login() {
  const { login, isAuthenticated, isLoading } = useAuth()
  const [showPassword, setShowPassword] = useState(false)
  const [apiError, setApiError] = useState('')
  const navigate = useNavigate(); const location = useLocation()
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({ resolver: zodResolver(schema) })
  if (!isLoading && isAuthenticated) return <Navigate to="/dashboard" replace />
  const submit = handleSubmit(async data => {
    setApiError('')
    try { await login(data); navigate(location.state?.from?.pathname || '/dashboard', { replace: true }) }
    catch (error) { setApiError(getApiErrorMessage(error)) }
  })
  return <AuthShell title="Welcome back" subtitle="Sign in to continue your interview preparation.">
    <form onSubmit={submit} className="space-y-5" noValidate>
      {apiError && <Alert message={apiError} />}
      <Input label="Email address" type="email" autoComplete="email" placeholder="you@example.com" error={errors.email?.message} {...register('email')} />
      <div className="relative"><Input label="Password" type={showPassword ? 'text' : 'password'} autoComplete="current-password" placeholder="Enter your password" error={errors.password?.message} className="pr-12" {...register('password')} />
        <button type="button" onClick={() => setShowPassword(value => !value)} aria-label={showPassword ? 'Hide password' : 'Show password'} className="absolute right-3 top-9 grid size-8 place-items-center rounded-lg text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500">{showPassword ? <FiEyeOff /> : <FiEye />}</button>
      </div>
      <Button type="submit" isLoading={isSubmitting} className="w-full">{isSubmitting ? 'Signing in…' : 'Sign in'}</Button>
    </form>
    <p className="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">New to InterviewAce? <Link to="/register" className="font-semibold text-indigo-600 hover:text-indigo-500 dark:text-indigo-400">Create an account</Link></p>
  </AuthShell>
}
