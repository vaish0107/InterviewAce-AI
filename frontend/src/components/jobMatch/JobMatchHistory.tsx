import { FiClock } from 'react-icons/fi'
import type { JobMatchAnalysis } from '../../types/jobMatch'
import { formatDate } from '../../utils/formatters'
import { Card } from '../common/Card'
import { LoadingSpinner } from '../common/LoadingSpinner'

export function JobMatchHistory({ history, selectedId, loading, onSelect }: { history: JobMatchAnalysis[]; selectedId?: number; loading: boolean; onSelect: (analysis: JobMatchAnalysis) => void }) {
  return <Card className="p-5"><div className="flex items-center gap-3"><span className="grid size-10 place-items-center rounded-xl bg-indigo-50 text-indigo-600 dark:bg-indigo-950/40 dark:text-indigo-300"><FiClock /></span><div><h3 className="font-bold">Match history</h3><p className="text-sm text-slate-500">Newest analysis first</p></div></div>
    {loading ? <LoadingSpinner label="Loading match history…" /> : history.length === 0 ? <p className="mt-6 rounded-xl border border-dashed border-slate-300 p-5 text-center text-sm text-slate-500 dark:border-slate-700">No job match history exists for this resume yet.</p> : <div className="mt-5 space-y-2">{history.map(item => <button key={item.id} type="button" onClick={() => onSelect(item)} className={`w-full rounded-xl border p-3 text-left transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 ${selectedId === item.id ? 'border-indigo-400 bg-indigo-50 dark:border-indigo-700 dark:bg-indigo-950/30' : 'border-slate-200 hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800/50'}`}><div className="flex items-center justify-between gap-3"><span className="font-bold text-indigo-600 dark:text-indigo-400">{item.matchPercentage}% match</span><span className="text-xs text-slate-500">{formatDate(item.createdAt)}</span></div><p className="mt-2 text-xs text-slate-500">{item.matchedSkills.length} matched · {item.missingSkills.length} missing</p></button>)}</div>}
  </Card>
}
