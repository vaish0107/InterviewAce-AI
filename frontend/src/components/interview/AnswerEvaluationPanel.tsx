import { FiAlertCircle, FiCheckCircle, FiInfo, FiRefreshCw, FiStar as FiSparkles, FiTarget } from 'react-icons/fi'
import type { ReactNode } from 'react'
import type { InterviewAnswerEvaluation } from '../../types/interview'
import { formatDate } from '../../utils/formatters'
import { Button } from '../common/Button'
import { Card } from '../common/Card'
import { LoadingSpinner } from '../common/LoadingSpinner'

interface Props {
  evaluation: InterviewAnswerEvaluation | null
  category: string
  hasAnswer: boolean
  loading: boolean
  evaluating: boolean
  onEvaluate: () => void
}

export function AnswerEvaluationPanel({ evaluation, category, hasAnswer, loading, evaluating, onEvaluate }: Props) {
  if (loading) return <Card className="p-5"><LoadingSpinner label="Loading saved evaluation…" /></Card>
  if (!evaluation) return <Card className="p-5 sm:p-6"><div className="flex items-start gap-3"><span className="grid size-11 shrink-0 place-items-center rounded-xl bg-violet-50 text-violet-600 dark:bg-violet-950/40 dark:text-violet-300"><FiSparkles /></span><div><h3 className="font-bold">AI-generated feedback</h3><p className="mt-1 text-sm leading-6 text-slate-500">Evaluate this saved answer against the question-specific rubric. Feedback is advisory and may be imperfect.</p><Button className="mt-4" onClick={onEvaluate} isLoading={evaluating} disabled={!hasAnswer}>{evaluating ? 'Evaluating answer…' : 'Evaluate Answer'}</Button>{!hasAnswer && <p className="mt-2 text-xs text-slate-500">Save an answer before requesting evaluation.</p>}</div></div></Card>
  if (evaluation.status === 'PROCESSING' || evaluation.status === 'PENDING') return <Card className="p-5"><LoadingSpinner label="Evaluating your saved answer…" /></Card>
  if (evaluation.status === 'FAILED') return <Card className="border-red-200 p-5 dark:border-red-900"><div className="flex gap-3"><FiAlertCircle className="mt-1 shrink-0 text-red-600" /><div><h3 className="font-bold">Evaluation could not be completed.</h3><p className="mt-2 text-sm text-slate-500">{evaluation.failureMessage || 'The AI evaluation service is currently unavailable.'}</p><Button className="mt-4" onClick={onEvaluate} isLoading={evaluating}><FiRefreshCw />Retry Evaluation</Button></div></div></Card>

  const hr = category === 'HR'
  const dimensions = [
    { label: 'Relevance', value: evaluation.relevanceScore ?? 0, max: 25 },
    { label: hr ? 'Structure / quality' : 'Correctness', value: evaluation.correctnessScore ?? 0, max: 35 },
    { label: 'Completeness', value: evaluation.completenessScore ?? 0, max: 25 },
    { label: 'Communication', value: evaluation.communicationScore ?? 0, max: 15 },
  ]
  return <div className="space-y-5"><Card className="overflow-hidden"><div className="grid gap-6 p-6 sm:grid-cols-[170px_1fr] sm:items-center"><div className="text-center"><p className="text-5xl font-bold text-violet-600 dark:text-violet-400">{evaluation.overallScore}<span className="text-xl text-slate-400"> / 100</span></p><p className="mt-2 text-sm font-semibold">Answer Evaluation Score</p></div><div><div className="flex items-center gap-2 text-violet-600 dark:text-violet-300"><FiSparkles /><span className="text-sm font-bold uppercase tracking-wide">AI-generated feedback</span></div><p className="mt-3 text-sm leading-6 text-slate-600 dark:text-slate-300">{evaluation.summary}</p>{hr && <p className="mt-3 text-xs text-slate-500">For HR questions, this focuses on relevance, structure, specificity, professionalism, and communication—not one objectively correct answer.</p>}<p className="mt-3 text-xs text-slate-400">Evaluated {formatDate(evaluation.evaluatedAt)}</p></div></div><div className="grid gap-3 border-t border-slate-200 p-5 sm:grid-cols-2 dark:border-slate-800">{dimensions.map(item => <div key={item.label}><div className="flex justify-between text-xs"><span>{item.label}</span><span className="font-bold">{item.value} / {item.max}</span></div><div className="mt-2 h-2 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-800"><div className="h-full rounded-full bg-violet-600" style={{ width: `${(item.value / item.max) * 100}%` }} /></div></div>)}</div></Card>
    <div className="grid gap-5 lg:grid-cols-3"><FeedbackList title="Strengths" items={evaluation.strengths} icon={<FiCheckCircle />} tone="positive" empty="No specific strengths were returned." /><FeedbackList title="Areas to Improve" items={evaluation.weaknesses} icon={<FiTarget />} tone="warning" empty="No specific weaknesses were returned." /><FeedbackList title="Missing Key Points" items={evaluation.missingKeyPoints} icon={<FiInfo />} tone="neutral" empty="No missing key points were returned." /></div>
    <Card className="p-5 sm:p-6"><h3 className="font-bold">Improved Answer Example</h3><p className="mt-3 whitespace-pre-wrap text-sm leading-7 text-slate-600 dark:text-slate-300">{evaluation.improvedAnswer}</p></Card>
    <div className="flex gap-3 rounded-xl border border-violet-200 bg-violet-50 p-4 text-sm leading-6 text-violet-900 dark:border-violet-900 dark:bg-violet-950/25 dark:text-violet-200"><FiInfo className="mt-1 shrink-0" /><p>{evaluation.evaluationNote}</p></div>
  </div>
}

function FeedbackList({ title, items, icon, tone, empty }: { title: string; items: string[]; icon: ReactNode; tone: 'positive' | 'warning' | 'neutral'; empty: string }) {
  const colors = { positive: 'text-emerald-600', warning: 'text-amber-600', neutral: 'text-blue-600' }
  return <Card className="p-5"><h3 className="font-bold">{title}</h3>{items.length ? <ul className="mt-4 space-y-3">{items.map((item, index) => <li key={`${index}-${item}`} className="flex gap-2 text-sm leading-6"><span className={`mt-1 shrink-0 ${colors[tone]}`}>{icon}</span><span>{item}</span></li>)}</ul> : <p className="mt-3 text-sm text-slate-500">{empty}</p>}</Card>
}
