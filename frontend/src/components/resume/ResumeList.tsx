import { Link } from 'react-router-dom'
import { FiBarChart2, FiEye, FiFileText, FiTrash2 } from 'react-icons/fi'
import type { Resume, ResumeStatus } from '../../types/resume'
import { formatDate, formatFileSize } from '../../utils/formatters'
import { Button } from '../common/Button'
import { Card } from '../common/Card'

const badgeStyles: Record<ResumeStatus, string> = {
  UPLOADED: 'bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300',
  PROCESSING: 'bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300',
  COMPLETED: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300',
  FAILED: 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-300',
}

function StatusBadge({ status }: { status: ResumeStatus }) {
  return <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${badgeStyles[status]}`}>{status.charAt(0) + status.slice(1).toLowerCase()}</span>
}

interface Props { resumes: Resume[]; analyzingId: number | null; onAnalyze: (resume: Resume) => void; onDelete: (resume: Resume) => void }

function Actions({ resume, analyzingId, onAnalyze, onDelete }: Props & { resume: Resume }) {
  const analyzing = analyzingId === resume.id
  return <div className="flex flex-wrap items-center gap-2">
    <Button type="button" variant="secondary" onClick={() => onAnalyze(resume)} isLoading={analyzing} disabled={analyzingId !== null} className="min-h-9 px-3 py-1.5">{analyzing ? 'Analyzing…' : <><FiBarChart2 />Analyze</>}</Button>
    <Link to={`/resumes/${resume.id}/analysis`} className="inline-flex min-h-9 items-center gap-2 rounded-xl px-3 py-1.5 text-sm font-semibold text-indigo-600 hover:bg-indigo-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 dark:text-indigo-400 dark:hover:bg-indigo-950/30"><FiEye />View analysis</Link>
    <button type="button" onClick={() => onDelete(resume)} disabled={analyzing} aria-label={`Delete ${resume.originalFileName}`} className="grid size-9 place-items-center rounded-xl text-slate-500 hover:bg-red-50 hover:text-red-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500 disabled:opacity-50 dark:hover:bg-red-950/30 dark:hover:text-red-300"><FiTrash2 /></button>
  </div>
}

export function ResumeList(props: Props) {
  return <>
    <Card className="hidden overflow-hidden md:block"><div className="overflow-x-auto"><table className="w-full min-w-[780px] text-left"><thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase tracking-wide text-slate-500 dark:border-slate-800 dark:bg-slate-900/60"><tr><th className="px-5 py-4">File name</th><th className="px-5 py-4">Size</th><th className="px-5 py-4">Status</th><th className="px-5 py-4">Uploaded</th><th className="px-5 py-4">Actions</th></tr></thead><tbody className="divide-y divide-slate-200 dark:divide-slate-800">{props.resumes.map(resume => <tr key={resume.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/30"><td className="px-5 py-4"><div className="flex items-center gap-3"><span className="grid size-10 shrink-0 place-items-center rounded-xl bg-red-50 text-red-600 dark:bg-red-950/40 dark:text-red-300"><FiFileText /></span><span className="max-w-52 truncate text-sm font-semibold" title={resume.originalFileName}>{resume.originalFileName}</span></div></td><td className="px-5 py-4 text-sm text-slate-500">{formatFileSize(resume.fileSize)}</td><td className="px-5 py-4"><StatusBadge status={resume.uploadStatus} /></td><td className="px-5 py-4 text-sm text-slate-500">{formatDate(resume.uploadedAt)}</td><td className="px-5 py-4"><Actions {...props} resume={resume} /></td></tr>)}</tbody></table></div></Card>
    <div className="grid gap-4 md:hidden">{props.resumes.map(resume => <Card key={resume.id} className="p-4"><div className="flex items-start gap-3"><span className="grid size-10 shrink-0 place-items-center rounded-xl bg-red-50 text-red-600 dark:bg-red-950/40 dark:text-red-300"><FiFileText /></span><div className="min-w-0 flex-1"><p className="truncate font-semibold">{resume.originalFileName}</p><p className="mt-1 text-sm text-slate-500">{formatFileSize(resume.fileSize)} · {formatDate(resume.uploadedAt)}</p></div><StatusBadge status={resume.uploadStatus} /></div><div className="mt-4 border-t border-slate-200 pt-4 dark:border-slate-800"><Actions {...props} resume={resume} /></div></Card>)}</div>
  </>
}
