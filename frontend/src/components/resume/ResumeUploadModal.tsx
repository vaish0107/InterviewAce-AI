import { useEffect, useRef, useState, type ChangeEvent, type DragEvent } from 'react'
import { FiFileText, FiUploadCloud, FiX } from 'react-icons/fi'
import { resumeService } from '../../services/resumeService'
import { getApiErrorMessage } from '../../utils/getApiErrorMessage'
import { formatFileSize } from '../../utils/formatters'
import { Alert } from '../common/Alert'
import { Button } from '../common/Button'

const MAX_SIZE = 5 * 1024 * 1024

function validate(file: File): string {
  if (!file.name.toLowerCase().endsWith('.pdf') || (file.type && file.type !== 'application/pdf')) return 'Select a PDF file.'
  if (file.size > MAX_SIZE) return 'The PDF must be 5 MB or smaller.'
  if (file.size === 0) return 'The selected file is empty.'
  return ''
}

export function ResumeUploadModal({ open, onClose, onUploaded }: { open: boolean; onClose: () => void; onUploaded: (message: string) => Promise<void> | void }) {
  const [file, setFile] = useState<File | null>(null); const [error, setError] = useState('')
  const [uploading, setUploading] = useState(false); const [progress, setProgress] = useState(0)
  const inputRef = useRef<HTMLInputElement>(null); const closeRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (!open) return
    closeRef.current?.focus()
    const closeOnEscape = (event: KeyboardEvent) => { if (event.key === 'Escape' && !uploading) onClose() }
    document.addEventListener('keydown', closeOnEscape); document.body.style.overflow = 'hidden'
    return () => { document.removeEventListener('keydown', closeOnEscape); document.body.style.overflow = '' }
  }, [open, onClose, uploading])
  useEffect(() => { if (!open) { setFile(null); setError(''); setProgress(0) } }, [open])

  if (!open) return null
  const select = (selected?: File) => { if (!selected) return; const message = validate(selected); setError(message); setFile(message ? null : selected) }
  const change = (event: ChangeEvent<HTMLInputElement>) => select(event.target.files?.[0])
  const drop = (event: DragEvent<HTMLDivElement>) => { event.preventDefault(); select(event.dataTransfer.files?.[0]) }
  const upload = async () => {
    if (!file || uploading) return
    setUploading(true); setError(''); setProgress(0)
    try { const result = await resumeService.uploadResume(file, setProgress); await onUploaded(result.message); onClose() }
    catch (requestError) { setError(getApiErrorMessage(requestError)) }
    finally { setUploading(false) }
  }
  return <div className="fixed inset-0 z-[60] grid place-items-center overflow-y-auto bg-slate-950/60 p-4" role="presentation" onMouseDown={event => { if (event.target === event.currentTarget && !uploading) onClose() }}>
    <section role="dialog" aria-modal="true" aria-labelledby="upload-title" className="w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-5 shadow-2xl sm:p-6 dark:border-slate-800 dark:bg-slate-900">
      <div className="flex items-start justify-between gap-4"><div><h2 id="upload-title" className="text-xl font-bold">Upload resume</h2><p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Choose a PDF up to 5 MB.</p></div><button ref={closeRef} type="button" disabled={uploading} onClick={onClose} aria-label="Close upload dialog" className="grid size-10 place-items-center rounded-xl text-slate-500 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 dark:hover:bg-slate-800"><FiX /></button></div>
      <div onDragOver={event => event.preventDefault()} onDrop={drop} className="mt-6 rounded-2xl border-2 border-dashed border-slate-300 p-8 text-center transition hover:border-indigo-400 dark:border-slate-700">
        <FiUploadCloud className="mx-auto text-3xl text-indigo-600 dark:text-indigo-400" aria-hidden="true" /><p className="mt-3 font-semibold">Drag and drop your PDF here</p><p className="mt-1 text-sm text-slate-500">or choose it from your device</p>
        <input ref={inputRef} type="file" accept="application/pdf,.pdf" onChange={change} className="sr-only" id="resume-file" />
        <Button type="button" variant="secondary" className="mt-5" onClick={() => inputRef.current?.click()} disabled={uploading}>Choose PDF</Button>
      </div>
      {file && <div className="mt-4 flex items-center gap-3 rounded-xl bg-slate-50 p-3 dark:bg-slate-800/70"><span className="grid size-10 place-items-center rounded-lg bg-red-50 text-red-600 dark:bg-red-950/40 dark:text-red-300"><FiFileText /></span><div className="min-w-0 flex-1"><p className="truncate text-sm font-semibold">{file.name}</p><p className="text-xs text-slate-500">{formatFileSize(file.size)}</p></div>{!uploading && <button type="button" onClick={() => setFile(null)} aria-label="Remove selected file" className="grid size-8 place-items-center rounded-lg hover:bg-slate-200 dark:hover:bg-slate-700"><FiX /></button>}</div>}
      {uploading && <div className="mt-4" role="status"><div className="mb-2 flex justify-between text-sm"><span>Uploading resume…</span><span>{progress}%</span></div><div className="h-2 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-700"><div className="h-full rounded-full bg-indigo-600 transition-all" style={{ width: `${progress}%` }} /></div></div>}
      {error && <div className="mt-4"><Alert message={error} /></div>}
      <div className="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end"><Button type="button" variant="secondary" onClick={onClose} disabled={uploading}>Cancel</Button><Button type="button" onClick={upload} isLoading={uploading} disabled={!file}>{uploading ? 'Uploading…' : 'Upload resume'}</Button></div>
    </section>
  </div>
}
