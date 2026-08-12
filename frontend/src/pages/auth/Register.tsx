import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { FiEye, FiEyeOff } from 'react-icons/fi'
import { useAuth } from '../../context/AuthContext'
import { getApiErrorMessage } from '../../utils/getApiErrorMessage'
import { AuthShell } from '../../components/auth/AuthShell'
import { Input } from '../../components/common/Input'
import { Button } from '../../components/common/Button'
import { Alert } from '../../components/common/Alert'

const schema = z.object({
  fullName: z.string().trim().min(1, 'Name is required').max(100),
  email: z.email('Enter a valid email address'),
  password: z.string().min(8, 'Password must contain at least 8 characters'),
  confirmPassword: z.string(),
}).refine(data => data.password === data.confirmPassword, { message: 'Passwords do not match', path: ['confirmPassword'] })
type FormData = z.infer<typeof schema>

export function Register() {
  const { register: createAccount, isAuthenticated, isLoading } = useAuth()
  const [showPassword, setShowPassword] = useState(false); const [apiError, setApiError] = useState(''); const navigate = useNavigate()
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({ resolver: zodResolver(schema) })
  if (!isLoading && isAuthenticated) return <Navigate to="/dashboard" replace />
  const submit = handleSubmit(async ({ fullName, email, password }) => {
    setApiError('')
    try { await createAccount({ fullName, email, password }); navigate('/dashboard', { replace: true }) }
    catch (error) { setApiError(getApiErrorMessage(error)) }
  })
  return <AuthShell title="Create your account" subtitle="Start building a more focused interview strategy.">
    <form onSubmit={submit} className="space-y-4" noValidate>
      {apiError && <Alert message={apiError} />}
      <Input label="Full name" autoComplete="name" placeholder="Jane Doe" error={errors.fullName?.message} {...register('fullName')} />
      <Input label="Email address" type="email" autoComplete="email" placeholder="you@example.com" error={errors.email?.message} {...register('email')} />
      <div className="relative"><Input label="Password" type={showPassword ? 'text' : 'password'} autoComplete="new-password" placeholder="At least 8 characters" error={errors.password?.message} className="pr-12" {...register('password')} />
        <button type="button" onClick={() => setShowPassword(value => !value)} aria-label={showPassword ? 'Hide passwords' : 'Show passwords'} className="absolute right-3 top-9 grid size-8 place-items-center rounded-lg text-slate-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500">{showPassword ? <FiEyeOff /> : <FiEye />}</button>
      </div>
      <Input label="Confirm password" type={showPassword ? 'text' : 'password'} autoComplete="new-password" placeholder="Repeat your password" error={errors.confirmPassword?.message} {...register('confirmPassword')} />
      <Button type="submit" isLoading={isSubmitting} className="w-full">{isSubmitting ? 'Creating account…' : 'Create account'}</Button>
    </form>
    <p className="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">Already have an account? <Link to="/login" className="font-semibold text-indigo-600 hover:text-indigo-500 dark:text-indigo-400">Sign in</Link></p>
  </AuthShell>
}
