import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { FiArrowRight, FiBarChart2, FiBriefcase, FiFilePlus, FiFileText, FiMessageSquare } from 'react-icons/fi'
import { useAuth } from '../../context/AuthContext'
import { Card } from '../../components/common/Card'
import { LoadingSpinner } from '../../components/common/LoadingSpinner'
import { Alert } from '../../components/common/Alert'
import { analyticsService } from '../../services/analyticsService'
import type { DashboardSummary } from '../../types/analytics'
import { getApiErrorMessage } from '../../utils/getApiErrorMessage'

const stats = [
  { label: 'Resumes', icon: FiFileText }, { label: 'Latest ATS Score', icon: FiBarChart2 },
  { label: 'Job Matches', icon: FiBriefcase }, { label: 'Interviews Completed', icon: FiMessageSquare },
]
const actions = [
  { title: 'Upload Resume', text: 'Add your latest resume to begin.', to: '/resumes', icon: FiFilePlus },
  { title: 'Analyze Resume', text: 'Understand your resume quality.', to: '/resumes', icon: FiBarChart2 },
  { title: 'Match a Job', text: 'Compare skills with a target role.', to: '/job-match', icon: FiBriefcase },
  { title: 'Start Interview', text: 'Practice for your next opportunity.', to: '/interviews', icon: FiMessageSquare },
]
const journey = ['Upload resume', 'Analyze resume', 'Match job description', 'Practice interview', 'Track improvement']

export function Dashboard() {
  const { user } = useAuth(); const firstName = user?.fullName.trim().split(/\s+/)[0] || 'there'
  const [summary, setSummary] = useState<DashboardSummary | null>(null); const [loading, setLoading] = useState(true); const [error, setError] = useState('')
  useEffect(() => { let active = true; analyticsService.getDashboardSummary().then(value => { if (active) setSummary(value) }).catch(requestError => { if (active) setError(getApiErrorMessage(requestError)) }).finally(() => { if (active) setLoading(false) }); return () => { active = false } }, [])
  const values: Record<string, string> = { Resumes: String(summary?.resumeCount ?? 0), 'Latest ATS Score': summary?.latestAtsScore == null ? 'No data yet' : `${summary.latestAtsScore} / 100`, 'Job Matches': String(summary?.jobMatchCount ?? 0), 'Interviews Completed': String(summary?.completedInterviewCount ?? 0) }
  return <div className="space-y-9">
    <section><p className="text-sm font-semibold text-indigo-600 dark:text-indigo-400">YOUR WORKSPACE</p><h2 className="mt-2 text-3xl font-bold tracking-tight">Welcome back, {firstName}</h2><p className="mt-2 text-slate-500 dark:text-slate-400">Keep building toward your next great interview.</p></section>
    <section aria-labelledby="overview-heading"><h3 id="overview-heading" className="sr-only">Overview</h3>{loading ? <LoadingSpinner label="Loading dashboard statistics..." /> : error ? <Alert message={error} /> : <><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{stats.map(({ label, icon: Icon }) => <Card key={label} className="p-5"><div className="flex items-start justify-between"><div><p className="text-sm text-slate-500 dark:text-slate-400">{label}</p><p className="mt-3 text-xl font-bold text-slate-700 dark:text-slate-200">{values[label]}</p></div><span className="grid size-10 place-items-center rounded-xl bg-indigo-50 text-indigo-600 dark:bg-indigo-500/10 dark:text-indigo-300"><Icon /></span></div></Card>)}</div><div className="mt-4 flex flex-wrap gap-x-6 gap-y-2 text-sm text-slate-500"><span>Evaluated answers: <strong className="text-slate-800 dark:text-slate-200">{summary?.evaluatedAnswerCount ?? 0}</strong></span><span>Interview average: <strong className="text-slate-800 dark:text-slate-200">{summary?.interviewAverageScore == null ? 'No data yet' : `${summary.interviewAverageScore.toFixed(1)} / 100`}</strong></span></div></>}</section>
    <section><div className="mb-4"><h3 className="text-xl font-bold">Quick actions</h3><p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Choose where you want to make progress.</p></div><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{actions.map(({ title, text, to, icon: Icon }) => <Link key={title} to={to} className="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-indigo-300 hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 dark:border-slate-800 dark:bg-slate-900 dark:hover:border-indigo-700"><Icon className="text-xl text-indigo-600 dark:text-indigo-400" /><h4 className="mt-5 font-semibold">{title}</h4><p className="mt-2 text-sm leading-6 text-slate-500 dark:text-slate-400">{text}</p><span className="mt-5 flex items-center gap-2 text-sm font-semibold text-indigo-600 dark:text-indigo-400">Explore <FiArrowRight className="transition group-hover:translate-x-1" /></span></Link>)}</div></section>
    <Card className="p-6 sm:p-8"><h3 className="text-xl font-bold">Your InterviewAce journey</h3><p className="mt-1 text-sm text-slate-500 dark:text-slate-400">A simple path from application to confident interview.</p><ol className="mt-7 grid gap-5 md:grid-cols-5">{journey.map((item, index) => <li key={item}><span className="grid size-9 place-items-center rounded-full bg-indigo-600 text-sm font-bold text-white">{index + 1}</span><p className="mt-3 text-sm font-medium">{item}</p></li>)}</ol></Card>
  </div>
}
