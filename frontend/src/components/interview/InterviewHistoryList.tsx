import { Link } from 'react-router-dom'
import { FiArrowRight, FiClock } from 'react-icons/fi'
import type { InterviewSession } from '../../types/interview'
import type { InterviewTrendPoint } from '../../types/analytics'
import { formatDate } from '../../utils/formatters'
import { Card } from '../common/Card'
import { EmptyState } from '../common/EmptyState'

const statusStyles = {
  CREATED: 'bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300',
  IN_PROGRESS: 'bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300',
  COMPLETED: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300',
  ABANDONED: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300',
}

export function InterviewHistoryList({ sessions, compact = false, trend = [] }: { sessions: InterviewSession[]; compact?: boolean; trend?: InterviewTrendPoint[] }) {
  if (!sessions.length) return <EmptyState title="No interview sessions yet" description="Start a deterministic practice session to build your history." />
  const averages = new Map(trend.map(point => [point.interviewId, point.averageScore]))
  return <div className="grid gap-4">{sessions.map(session => { const average = averages.get(session.id); return <Card key={session.id} className="p-5"><div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between"><div className="flex min-w-0 gap-3"><span className="grid size-11 shrink-0 place-items-center rounded-xl bg-indigo-50 text-indigo-600 dark:bg-indigo-950/40 dark:text-indigo-300"><FiClock /></span><div className="min-w-0"><div className="flex flex-wrap items-center gap-2"><h3 className="font-bold">{session.interviewType} interview</h3><span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${statusStyles[session.status]}`}>{session.status.replace('_', ' ')}</span>{average != null && <span className="rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700 dark:bg-indigo-950/40 dark:text-indigo-300">Avg {average.toFixed(1)}</span>}</div><p className="mt-1 text-sm text-slate-500">{session.difficulty} · {session.answeredQuestions}/{session.totalQuestions} answered · {formatDate(session.createdAt)}</p>{!compact && <p className="mt-1 truncate text-xs text-slate-400">{session.resumeFileName || 'No resume attached'}</p>}</div></div><Link to={`/interviews/${session.id}`} className="inline-flex min-h-10 shrink-0 items-center justify-center gap-2 rounded-xl px-3 text-sm font-semibold text-indigo-600 hover:bg-indigo-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 dark:text-indigo-400 dark:hover:bg-indigo-950/30">{session.status === 'COMPLETED' ? 'Review' : 'Continue'}<FiArrowRight /></Link></div></Card> })}</div>
}
