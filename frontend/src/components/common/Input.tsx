import { forwardRef, type InputHTMLAttributes } from 'react'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> { label: string; error?: string }

export const Input = forwardRef<HTMLInputElement, InputProps>(({ label, error, id, className = '', ...props }, ref) => {
  const inputId = id || props.name
  return <div className="space-y-1.5">
    <label htmlFor={inputId} className="block text-sm font-medium text-slate-700 dark:text-slate-200">{label}</label>
    <input ref={ref} id={inputId} aria-invalid={Boolean(error)} aria-describedby={error ? `${inputId}-error` : undefined}
      className={`min-h-11 w-full rounded-xl border bg-white px-3.5 py-2.5 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-3 focus:ring-indigo-500/15 dark:bg-slate-900 dark:text-white ${error ? 'border-red-500' : 'border-slate-300 dark:border-slate-700'} ${className}`} {...props} />
    {error && <p id={`${inputId}-error`} className="text-sm text-red-600 dark:text-red-400">{error}</p>}
  </div>
})
Input.displayName = 'Input'
