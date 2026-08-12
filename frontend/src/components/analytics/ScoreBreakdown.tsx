const dimensions = [
  ['Relevance', 'averageRelevance', 25], ['Correctness', 'averageCorrectness', 35],
  ['Completeness', 'averageCompleteness', 25], ['Communication', 'averageCommunication', 15],
] as const

type Scores = { averageRelevance: number | null; averageCorrectness: number | null; averageCompleteness: number | null; averageCommunication: number | null }
export function ScoreBreakdown({ scores }: { scores: Scores }) {
  return <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">{dimensions.map(([label, key, maximum]) => { const value = scores[key]; return <div key={key} className="rounded-xl bg-slate-50 p-4 dark:bg-slate-800/60"><p className="text-xs text-slate-500">{label}</p><p className="mt-1 text-xl font-bold">{value == null ? 'No data' : `${value.toFixed(1)} / ${maximum}`}</p>{value != null && <div className="mt-3 h-1.5 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-700" role="img" aria-label={`${label}: ${value.toFixed(1)} out of ${maximum}`}><div className="h-full bg-indigo-600" style={{ width: `${(value / maximum) * 100}%` }} /></div>}</div> })}</div>
}
