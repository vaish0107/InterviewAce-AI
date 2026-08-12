import { useEffect, useRef } from 'react'
import { FiAlertTriangle, FiX } from 'react-icons/fi'
import type { Resume } from '../../types/resume'
import { Button } from '../common/Button'

export function DeleteResumeDialog({ resume, deleting, onCancel, onConfirm }: { resume: Resume | null; deleting: boolean; onCancel: () => void; onConfirm: () => void }) {
  const cancelRef = useRef<HTMLButtonElement>(null)
  useEffect(() => {
    if (!resume) return
    cancelRef.current?.focus()
    const escape = (event: KeyboardEvent) => { if (event.key === 'Escape' && !deleting) onCancel() }
    document.addEventListener('keydown', escape)
    return () => document.removeEventListener('keydown', escape)
  }, [resume, deleting, onCancel])
  if (!resume) return null
  return <div className="fixed inset-0 z-[60] grid place-items-center bg-slate-950/60 p-4">
    <section role="alertdialog" aria-modal="true" aria-labelledby="delete-title" aria-describedby="delete-description" className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 shadow-2xl dark:border-slate-800 dark:bg-slate-900">
      <div className="flex items-start justify-between"><span className="grid size-11 place-items-center rounded-xl bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300"><FiAlertTriangle /></span><button type="button" onClick={onCancel} disabled={deleting} aria-label="Close delete dialog" className="grid size-9 place-items-center rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800"><FiX /></button></div>
      <h2 id="delete-title" className="mt-5 text-xl font-bold">Delete this resume?</h2><p id="delete-description" className="mt-2 text-sm leading-6 text-slate-500 dark:text-slate-400"><strong className="text-slate-700 dark:text-slate-200">{resume.originalFileName}</strong> will be permanently removed. Saved analysis linked to it may also become unavailable.</p>
      <div className="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end"><Button ref={cancelRef} type="button" variant="secondary" disabled={deleting} onClick={onCancel}>Cancel</Button><Button type="button" isLoading={deleting} onClick={onConfirm} className="bg-red-600 hover:bg-red-700">{deleting ? 'Deleting…' : 'Delete resume'}</Button></div>
    </section>
  </div>
}
