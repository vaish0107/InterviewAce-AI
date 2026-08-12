import { FiInfo } from 'react-icons/fi'
import type { JobMatchAnalysis } from '../../types/jobMatch'
import { Card } from '../common/Card'

export function JobMatchSummary({ analysis }: { analysis: JobMatchAnalysis }) {
  const score = Math.max(0, Math.min(100, analysis.matchPercentage))
  const metrics = [
    { label: 'Matched skills', value: analysis.matchedSkills.length },
    { label: 'Required skills', value: analysis.jobSkillCount },
    { label: 'Additional skills', value: analysis.additionalResumeSkills.length },
  ]
  return <Card className="overflow-hidden"><div className="grid gap-6 p-6 sm:grid-cols-[180px_1fr] sm:items-center">
    <div className="relative mx-auto grid size-40 place-items-center rounded-full" style={{ background: `conic-gradient(#4f46e5 ${score * 3.6}deg, rgba(148,163,184,.2) 0deg)` }} role="img" aria-label={`${score} percent skill match`}><div className="grid size-32 place-items-center rounded-full bg-white text-center dark:bg-slate-900"><div><p className="text-4xl font-bold text-indigo-600 dark:text-indigo-400">{score}%</p><p className="mt-1 text-xs font-medium text-slate-500">Skill match</p></div></div></div>
    <div><p className="text-sm font-semibold uppercase tracking-wide text-indigo-600 dark:text-indigo-400">Match result</p><h3 className="mt-2 text-2xl font-bold">Resume-to-role alignment</h3><p className="mt-2 text-sm leading-6 text-slate-500 dark:text-slate-400">A comparison of explicitly detected skills, not a hiring probability.</p><div className="mt-5 grid grid-cols-3 gap-2">{metrics.map(metric => <div key={metric.label} className="rounded-xl bg-slate-50 p-3 text-center dark:bg-slate-800/60"><p className="text-xl font-bold">{metric.value}</p><p className="mt-1 text-xs text-slate-500">{metric.label}</p></div>)}</div></div>
  </div><div className="flex gap-3 border-t border-slate-200 bg-blue-50/70 p-4 text-sm leading-6 text-blue-900 dark:border-slate-800 dark:bg-blue-950/25 dark:text-blue-200"><FiInfo className="mt-1 shrink-0" /><p>{analysis.matchingNote}</p></div></Card>
}
