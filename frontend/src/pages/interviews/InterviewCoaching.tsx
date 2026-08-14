import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { FiArrowLeft, FiRefreshCw } from 'react-icons/fi'
import { coachingService } from '../../services/coachingService'
import type { InterviewCoachingReport } from '../../types/coaching'
import { getApiErrorMessage } from '../../utils/getApiErrorMessage'
import { Alert } from '../../components/common/Alert'
import { Button } from '../../components/common/Button'
import { Card } from '../../components/common/Card'
import { LoadingSpinner } from '../../components/common/LoadingSpinner'
import { CoachingFocusAreaCard } from '../../components/coaching/CoachingFocusAreaCard'

export function InterviewCoaching() {
  const id = Number(useParams().id)
  const navigate = useNavigate()
  const [report, setReport] = useState<InterviewCoachingReport | null>(null)
  const [loading, setLoading] = useState(true)
  const [generating, setGenerating] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    coachingService.getCoaching(id)
      .then(value => { if (active) setReport(value) })
      .catch(requestError => { if (active) setError(getApiErrorMessage(requestError)) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id])

  const generate = async (regenerate = false) => {
    if (regenerate && !window.confirm('Regeneration may produce slightly different wording. Continue?')) return
    setGenerating(true)
    setError('')
    try { setReport(await coachingService.generateCoaching(id)) }
    catch (requestError) {
      setError(getApiErrorMessage(requestError) || 'Coaching plan could not be generated.')
      const saved = await coachingService.getCoaching(id).catch(() => null)
      if (saved) setReport(saved)
    } finally { setGenerating(false) }
  }

  if (loading) return <LoadingSpinner label="Loading saved coaching..." />
  return <main className="mx-auto max-w-5xl space-y-6">
    <div className="flex flex-wrap items-center justify-between gap-3"><Link to={`/interviews/${id}`} className="inline-flex items-center gap-2 text-sm font-semibold text-indigo-600"><FiArrowLeft />Back to summary</Link><Link to={`/interviews/${id}/replay`} className="text-sm font-semibold text-indigo-600">Replay Interview</Link></div>
    <div><h1 className="text-3xl font-bold">Interview Coaching</h1><p className="mt-2 text-slate-500">Generate a personalized coaching plan from your evaluated interview answers.</p></div>
    {error && <Alert message={error} />}
    {!report ? <EmptyCoaching generating={generating} onGenerate={() => void generate()} /> : report.status === 'FAILED' ? <FailedCoaching report={report} generating={generating} onRetry={() => void generate()} /> : <CompletedCoaching report={report} generating={generating} onRegenerate={() => void generate(true)} onStartNew={() => navigate('/interviews')} />}
  </main>
}

function EmptyCoaching({ generating, onGenerate }: { generating: boolean; onGenerate: () => void }) {
  return <Card className="p-6"><h2 className="text-xl font-bold">No coaching plan yet</h2><p className="mt-2 text-sm text-slate-500">No coaching plan has been generated for this interview yet. Evaluate at least one answer before generating a plan.</p><Button className="mt-5" isLoading={generating} disabled={generating} onClick={onGenerate}>{generating ? 'Generating coaching plan...' : 'Generate Coaching Plan'}</Button></Card>
}

function FailedCoaching({ report, generating, onRetry }: { report: InterviewCoachingReport; generating: boolean; onRetry: () => void }) {
  return <Card className="p-6"><h2 className="text-xl font-bold">Coaching plan could not be generated.</h2><p className="mt-2 text-sm text-slate-500">{report.failureMessage || 'The coaching service is temporarily unavailable.'}</p><Button className="mt-5" isLoading={generating} onClick={onRetry}>Retry</Button></Card>
}

function CompletedCoaching({ report, generating, onRegenerate, onStartNew }: { report: InterviewCoachingReport; generating: boolean; onRegenerate: () => void; onStartNew: () => void }) {
  const focusAreas = report.primaryFocusAreas ?? []
  const recommendations = report.practiceRecommendations ?? []
  const revisionTopics = report.revisionTopics ?? []
  const communicationTips = report.communicationTips ?? []
  const practicePlan = report.nextPracticePlan ?? []
  return <><Card className="p-6"><p className="text-xs font-bold uppercase tracking-wide text-indigo-600">Coaching Summary</p><h2 className="mt-3 text-xl font-bold">Your current practice focus</h2><p className="mt-3 leading-7 text-slate-600 dark:text-slate-300">{report.summary || 'No coaching summary is available.'}</p></Card><section><h2 className="text-xl font-bold">Primary Focus Areas</h2>{focusAreas.length ? <div className="mt-4 grid gap-4 md:grid-cols-2">{focusAreas.map((area, index) => <CoachingFocusAreaCard key={`${area.title || 'focus'}-${index}`} area={area} />)}</div> : <EmptySection message="No primary focus areas available." />}</section><div className="grid gap-5 lg:grid-cols-2"><ListCard title="Practice Recommendations" items={recommendations} /><ListCard title="Revision Topics" items={revisionTopics} /><ListCard title="Communication Tips" items={communicationTips} /><Card className="p-5"><h2 className="text-lg font-bold">Next Practice Plan</h2>{practicePlan.length ? <ol className="mt-4 space-y-4">{[...practicePlan].sort((a, b) => (a.order ?? 0) - (b.order ?? 0)).map((item, index) => <li key={`${item.order ?? index}-${item.activity ?? 'activity'}`} className="flex gap-3"><span className="font-bold text-indigo-600">{item.order ?? index + 1}.</span><div><p className="font-semibold">{item.activity || 'Practice activity'}</p><p className="text-sm text-slate-500">Focus: {item.focus || 'Not specified'}{item.suggestedQuestionCount != null ? ` · ${item.suggestedQuestionCount} questions` : ''}</p></div></li>)}</ol> : <p className="mt-3 text-sm text-slate-500">No next practice plan available.</p>}</Card></div><Card className="p-5"><p className="text-sm text-slate-600 dark:text-slate-300">{report.coachingNote || 'This plan is intended for interview practice guidance.'}</p><div className="mt-4 flex flex-wrap gap-3"><Button onClick={onStartNew}>Start New Interview</Button><Button variant="secondary" isLoading={generating} onClick={onRegenerate}><FiRefreshCw />Regenerate Coaching</Button></div></Card></>
}

function ListCard({ title, items = [] }: { title: string; items?: string[] }) {
  return <Card className="p-5"><h2 className="text-lg font-bold">{title}</h2>{items.length ? <ul className="mt-4 list-disc space-y-2 pl-5 text-sm text-slate-600 dark:text-slate-300">{items.map((item, index) => <li key={`${item}-${index}`}>{item}</li>)}</ul> : <p className="mt-3 text-sm text-slate-500">No {title.toLowerCase()} available.</p>}</Card>
}

function EmptySection({ message }: { message: string }) { return <p className="mt-3 rounded-xl bg-slate-50 p-4 text-sm text-slate-500 dark:bg-slate-800/60">{message}</p> }
