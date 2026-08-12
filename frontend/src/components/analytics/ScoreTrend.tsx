import type { InterviewTrendPoint } from '../../types/analytics'
import { formatDate } from '../../utils/formatters'
import { Card } from '../common/Card'

export function ScoreTrend({ points }: { points: InterviewTrendPoint[] }) {
  if (!points.length) return <Card className="p-5 sm:p-6"><h3 className="font-bold">Score trend</h3><p className="mt-3 text-sm text-slate-500">Complete and evaluate interviews to build a trend.</p></Card>
  const width = 600; const height = 180; const pad = 20
  const coordinates = points.map((point, index) => ({ ...point, x: points.length === 1 ? width / 2 : pad + index * (width - 2 * pad) / (points.length - 1), y: height - pad - point.averageScore * (height - 2 * pad) / 100 }))
  return <Card className="overflow-hidden p-5 sm:p-6"><h3 className="font-bold">Score trend</h3><p className="mt-1 text-sm text-slate-500">Completed interviews, oldest to newest.</p><div className="mt-5 overflow-x-auto"><svg viewBox={`0 0 ${width} ${height}`} className="min-w-[32rem]" role="img" aria-label={`Interview average score trend: ${points.map(point => `${point.averageScore.toFixed(1)}`).join(', ')}`}><line x1={pad} y1={height - pad} x2={width - pad} y2={height - pad} stroke="currentColor" className="text-slate-300 dark:text-slate-700" /><polyline points={coordinates.map(point => `${point.x},${point.y}`).join(' ')} fill="none" stroke="currentColor" strokeWidth="4" className="text-indigo-600" />{coordinates.map(point => <circle key={point.interviewId} cx={point.x} cy={point.y} r="5" fill="currentColor" className="text-indigo-600"><title>{`${formatDate(point.completedAt)}: ${point.averageScore.toFixed(1)}`}</title></circle>)}</svg></div><ul className="mt-3 grid gap-2 text-xs text-slate-500 sm:grid-cols-2">{points.map(point => <li key={point.interviewId}>{formatDate(point.completedAt)}: <strong className="text-slate-700 dark:text-slate-200">{point.averageScore.toFixed(1)}</strong></li>)}</ul></Card>
}
