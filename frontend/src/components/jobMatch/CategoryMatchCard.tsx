import type { JobMatchCategory } from '../../types/jobMatch'
import { Card } from '../common/Card'

const specialNames: Record<string, string> = { cloud_devops: 'Cloud / DevOps', ai_data: 'AI / Data' }
function titleFor(key: string) { return specialNames[key] || key.split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ') }

function Skills({ label, skills, tone }: { label: string; skills: string[]; tone: 'matched' | 'missing' | 'neutral' }) {
  const styles = { matched: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300', missing: 'bg-amber-50 text-amber-800 dark:bg-amber-950/40 dark:text-amber-300', neutral: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300' }
  return <div><p className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</p><div className="mt-2 flex flex-wrap gap-2">{skills.length ? skills.map(skill => <span key={skill} className={`rounded-full px-2.5 py-1 text-xs font-medium ${styles[tone]}`}>{skill}</span>) : <span className="text-sm text-slate-400">None</span>}</div></div>
}

export function CategoryMatchCard({ name, category }: { name: string; category: JobMatchCategory }) {
  return <Card className="p-5"><div className="flex items-center justify-between gap-3"><h4 className="font-bold">{titleFor(name)}</h4><span className="rounded-full bg-indigo-50 px-3 py-1 text-sm font-bold text-indigo-700 dark:bg-indigo-950/40 dark:text-indigo-300">{category.matchPercentage}%</span></div><div className="mt-4 h-2 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-800"><div className="h-full rounded-full bg-indigo-600" style={{ width: `${Math.max(0, Math.min(100, category.matchPercentage))}%` }} /></div><div className="mt-5 space-y-4"><Skills label="Required" skills={category.requiredSkills} tone="neutral" /><Skills label="Matched" skills={category.matchedSkills} tone="matched" /><Skills label="Missing" skills={category.missingSkills} tone="missing" /></div></Card>
}
