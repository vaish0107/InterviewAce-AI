import { Card } from '../common/Card'

export function SkillPerformanceList({ scores }: { scores: Record<string, number> }) {
  const entries = Object.entries(scores)
  return <Card className="p-5 sm:p-6"><h3 className="font-bold">Skill performance</h3>{entries.length ? <ul className="mt-4 divide-y divide-slate-200 dark:divide-slate-800">{entries.map(([skill, score]) => <li key={skill} className="flex items-center justify-between gap-4 py-3 text-sm"><span className="truncate">{skill}</span><span className="font-bold">{score.toFixed(1)}</span></li>)}</ul> : <p className="mt-3 text-sm text-slate-500">No skill-specific performance data yet.</p>}</Card>
}
