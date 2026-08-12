import { useEffect, useState } from 'react'
import type { InterviewSession } from '../../types/interview'
import type { InterviewProgress } from '../../types/analytics'
import { interviewService } from '../../services/interviewService'
import { analyticsService } from '../../services/analyticsService'
import { getApiErrorMessage } from '../../utils/getApiErrorMessage'
import { Alert } from '../../components/common/Alert'
import { Card } from '../../components/common/Card'
import { LoadingSpinner } from '../../components/common/LoadingSpinner'
import { InterviewHistoryList } from '../../components/interview/InterviewHistoryList'
import { ScoreBreakdown } from '../../components/analytics/ScoreBreakdown'
import { CategoryPerformanceCard } from '../../components/analytics/CategoryPerformanceCard'
import { SkillPerformanceList } from '../../components/analytics/SkillPerformanceList'
import { ScoreTrend } from '../../components/analytics/ScoreTrend'

export function InterviewHistory() {
  const [sessions, setSessions] = useState<InterviewSession[]>([]); const [progress, setProgress] = useState<InterviewProgress | null>(null); const [loading, setLoading] = useState(true); const [error, setError] = useState('')
  useEffect(() => { let active = true; Promise.all([interviewService.getInterviews(), analyticsService.getInterviewProgress()]).then(([sessionValues, progressValue]) => { if (active) { setSessions(sessionValues); setProgress(progressValue) } }).catch(requestError => { if (active) setError(getApiErrorMessage(requestError)) }).finally(() => { if (active) setLoading(false) }); return () => { active = false } }, [])
  return <div><h2 className="text-3xl font-bold tracking-tight">Interview History & Progress</h2><p className="mt-2 text-slate-500">Review saved answers and track measured progress from completed evaluations.</p><div className="mt-8">{loading ? <LoadingSpinner label="Loading interview progress..." /> : error ? <Alert message={error} /> : <div className="space-y-8">{progress && <><section aria-labelledby="progress-heading"><h3 id="progress-heading" className="text-xl font-bold">Overall interview progress</h3><div className="mt-4 grid gap-4 sm:grid-cols-2 xl:grid-cols-4"><Metric label="Total interviews" value={progress.totalInterviews} /><Metric label="Completed" value={progress.completedInterviews} /><Metric label="Evaluated answers" value={progress.totalEvaluatedAnswers} /><Metric label="Average score" value={progress.overallAverageScore == null ? 'No data' : `${progress.overallAverageScore.toFixed(1)} / 100`} /></div></section><Card className="p-5 sm:p-6"><h3 className="font-bold">Rubric averages</h3><div className="mt-5"><ScoreBreakdown scores={progress} /></div><div className="mt-5 grid gap-3 sm:grid-cols-2"><Area label="Strongest measured category" value={progress.strongestCategory} /><Area label="Lowest measured category" value={progress.weakestCategory} /></div></Card><div className="grid gap-5 lg:grid-cols-2"><CategoryPerformanceCard scores={progress.categoryAverages} /><SkillPerformanceList scores={progress.skillAverages} /></div><ScoreTrend points={progress.scoreTrend} /></>}<section><h3 className="text-xl font-bold">Sessions</h3><div className="mt-4"><InterviewHistoryList sessions={sessions} trend={progress?.scoreTrend || []} /></div></section></div>}</div></div>
}

function Metric({ label, value }: { label: string; value: string | number }) { return <Card className="p-5"><p className="text-sm text-slate-500">{label}</p><p className="mt-2 text-2xl font-bold">{value}</p></Card> }
function Area({ label, value }: { label: string; value: string | null }) { return <div className="rounded-xl bg-slate-50 p-4 dark:bg-slate-800/60"><p className="text-xs text-slate-500">{label}</p><p className="mt-1 font-bold">{value || 'Not enough category data'}</p></div> }
