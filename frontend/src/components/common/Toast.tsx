import { useEffect } from 'react'
import { FiCheckCircle, FiX, FiXCircle } from 'react-icons/fi'

export interface ToastMessage { type: 'success' | 'error'; message: string }

export function Toast({ toast, onClose }: { toast: ToastMessage | null; onClose: () => void }) {
  useEffect(() => {
    if (!toast) return
    const timer = window.setTimeout(onClose, 4500)
    return () => window.clearTimeout(timer)
  }, [toast, onClose])
  if (!toast) return null
  const success = toast.type === 'success'
  return <div role={success ? 'status' : 'alert'} className={`fixed bottom-5 right-5 z-[70] flex max-w-sm items-start gap-3 rounded-xl border p-4 shadow-xl ${success ? 'border-emerald-200 bg-white text-emerald-800 dark:border-emerald-900 dark:bg-slate-900 dark:text-emerald-300' : 'border-red-200 bg-white text-red-800 dark:border-red-900 dark:bg-slate-900 dark:text-red-300'}`}>
    {success ? <FiCheckCircle className="mt-0.5 shrink-0" /> : <FiXCircle className="mt-0.5 shrink-0" />}
    <span className="text-sm font-medium">{toast.message}</span>
    <button type="button" onClick={onClose} aria-label="Dismiss notification" className="grid size-6 shrink-0 place-items-center rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500"><FiX /></button>
  </div>
}
