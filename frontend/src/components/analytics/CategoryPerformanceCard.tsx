import { Card } from '../common/Card'

const labels: Record<string, string> = { TECHNICAL: 'Technical', HR: 'HR', PROJECT: 'Project' }

export function CategoryPerformanceCard({ scores }: { scores: Record<string, number> }) {
  const entries = Object.entries(scores)
  if (!entries.length) return <Card className="p-5"><h3 className="font-bold">Category performance</h3><p className="mt-3 text-sm text-slate-500">No evaluated category data yet.</p></Card>
  return <Card className="p-5 sm:p-6"><h3 className="font-bold">Category performance</h3><div className="mt-5 space-y-4">{entries.map(([category, score]) => <div key={category}><div className="flex justify-between text-sm"><span>{labels[category] || category}</span><span className="font-bold">{score.toFixed(1)} / 100</span></div><div className="mt-2 h-2 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-800" role="img" aria-label={`${labels[category] || category}: ${score.toFixed(1)} out of 100`}><div className="h-full rounded-full bg-indigo-600" style={{ width: `${Math.max(0, Math.min(100, score))}%` }} /></div></div>)}</div></Card>
}
