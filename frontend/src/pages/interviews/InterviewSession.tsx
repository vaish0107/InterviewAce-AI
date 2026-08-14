import axios from 'axios'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { FiArrowLeft, FiCheck, FiChevronLeft, FiChevronRight, FiCpu } from 'react-icons/fi'
import type { InterviewAnswerEvaluation, InterviewSession as InterviewSessionType } from '../../types/interview'
import { interviewService } from '../../services/interviewService'
import { formatDate } from '../../utils/formatters'
import { getApiErrorMessage } from '../../utils/getApiErrorMessage'
import { Alert } from '../../components/common/Alert'
import { Button } from '../../components/common/Button'
import { Card } from '../../components/common/Card'
import { LoadingSpinner } from '../../components/common/LoadingSpinner'
import { AnswerEvaluationPanel } from '../../components/interview/AnswerEvaluationPanel'
import type { InterviewSummary } from '../../types/analytics'
import { analyticsService } from '../../services/analyticsService'
import { ScoreBreakdown } from '../../components/analytics/ScoreBreakdown'
import { CategoryPerformanceCard } from '../../components/analytics/CategoryPerformanceCard'
import { SkillPerformanceList } from '../../components/analytics/SkillPerformanceList'

export function InterviewSession() {
  const sessionId = Number(useParams().id)
  const [session, setSession] = useState<InterviewSessionType | null>(null)
  const [index, setIndex] = useState(0)
  const [answer, setAnswer] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [completing, setCompleting] = useState(false)
  const [error, setError] = useState('')
  const [saved, setSaved] = useState(false)
  const [evaluation, setEvaluation] = useState<InterviewAnswerEvaluation | null>(null)
  const [evaluationLoading, setEvaluationLoading] = useState(false)
  const [evaluating, setEvaluating] = useState(false)
  const [generatingFollowUp, setGeneratingFollowUp] = useState(false)
  const [followUpMessage, setFollowUpMessage] = useState('')

  useEffect(() => {
    let active = true
    interviewService.getInterview(sessionId).then(value => { if (active) setSession(value) }).catch(requestError => { if (active) setError(getApiErrorMessage(requestError)) }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [sessionId])

  const question = session?.questions[index]
  useEffect(() => { setAnswer(question?.answerText || ''); setSaved(false); setFollowUpMessage('') }, [question?.id, question?.answerText])
  useEffect(() => {
    let active = true
    setEvaluation(null)
    if (!question?.id || !question.answerText?.trim()) return () => { active = false }
    setEvaluationLoading(true)
    interviewService.getAnswerEvaluation(sessionId, question.id)
      .then(value => { if (active) setEvaluation(value) })
      .catch(requestError => { if (active && !(axios.isAxiosError(requestError) && requestError.response?.status === 404)) setError(getApiErrorMessage(requestError)) })
      .finally(() => { if (active) setEvaluationLoading(false) })
    return () => { active = false }
  }, [question?.id, question?.answerText, sessionId])

  const save = async () => {
    if (!session || !question || !answer.trim()) { setError('Answer is required before saving.'); return }
    setSaving(true); setError(''); setSaved(false)
    try { const updated = await interviewService.submitAnswer(session.id, question.id, answer); setSession(updated); setEvaluation(null); setSaved(true) }
    catch (requestError) { setError(getApiErrorMessage(requestError)) }
    finally { setSaving(false) }
  }
  const evaluate = async () => {
    if (!session || !question?.answerText?.trim()) return
    setEvaluating(true); setError('')
    try { setEvaluation(await interviewService.evaluateAnswer(session.id, question.id)) }
    catch (requestError) {
      try { setEvaluation(await interviewService.getAnswerEvaluation(session.id, question.id)) }
      catch { setError(getApiErrorMessage(requestError)) }
    } finally { setEvaluating(false) }
  }
  const complete = async () => {
    if (!session || !window.confirm('Complete this interview? You will no longer be able to edit answers.')) return
    setCompleting(true); setError('')
    try { setSession(await interviewService.completeInterview(session.id)) }
    catch (requestError) { setError(getApiErrorMessage(requestError)) }
    finally { setCompleting(false) }
  }
  const askFollowUp = async () => {
    if (!session || !question?.answerText?.trim()) return
    setGeneratingFollowUp(true); setError(''); setFollowUpMessage('')
    try {
      const result = await interviewService.generateFollowUp(session.id, question.id)
      if (!result.created || !result.question) setFollowUpMessage(result.reason || 'No additional follow-up is needed for this answer.')
      else { const refreshed = await interviewService.getInterview(session.id); setSession(refreshed); setIndex(refreshed.questions.findIndex(value => value.id === result.question?.id)) }
    } catch (requestError) { setFollowUpMessage('Follow-up could not be generated. You can continue to the next question.'); setError(getApiErrorMessage(requestError)) }
    finally { setGeneratingFollowUp(false) }
  }

  if (loading) return <LoadingSpinner label="Loading interview session..." />
  if (!session || !question) return <div><Alert message={error || 'Interview session has no questions.'} /><Link to="/interviews" className="mt-4 inline-flex text-sm font-semibold text-indigo-600">Back to interviews</Link></div>
  if (session.status === 'COMPLETED') return <CompletedSession session={session} index={index} setIndex={setIndex} evaluation={evaluation} evaluationLoading={evaluationLoading} evaluating={evaluating} onEvaluate={() => void evaluate()} error={error} />

  const actualTotal = session.questions.length
  const adaptiveTotal = session.questions.filter(value => value.adaptive).length
  const progress = Math.round((session.answeredQuestions / actualTotal) * 100)
  return <div className="mx-auto max-w-4xl space-y-6">
    <div><Link to="/interviews" className="inline-flex items-center gap-2 text-sm font-semibold text-indigo-600 dark:text-indigo-400"><FiArrowLeft />Back to interviews</Link><div className="mt-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between"><div><p className="text-sm text-slate-500">Question {index + 1} of {actualTotal} · Base questions {session.totalQuestions} · Adaptive follow-ups {adaptiveTotal}</p><h2 className="mt-1 text-2xl font-bold">{session.interviewType} Interview</h2></div><p className="text-sm font-semibold">Answered {session.answeredQuestions} / {actualTotal}</p></div><div className="mt-4 h-2 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-800"><div className="h-full rounded-full bg-indigo-600 transition-all" style={{ width: `${progress}%` }} /></div></div>
    {error && <Alert message={error} />}
    <Card className="p-5 sm:p-7"><div className="flex flex-wrap gap-2">{question.adaptive && <Badge>ADAPTIVE FOLLOW-UP</Badge>}<Badge>{question.category}</Badge>{question.skill && <Badge>{question.skill}</Badge>}{question.focusArea && <Badge>{question.focusArea}</Badge>}<Badge>{question.difficulty}</Badge></div><h3 className="mt-6 text-xl font-bold leading-8">{question.questionText}</h3><label htmlFor="interview-answer" className="mt-7 block text-sm font-medium">Your answer</label><textarea id="interview-answer" value={answer} onChange={event => { setAnswer(event.target.value); setSaved(false) }} rows={9} placeholder="Write your answer here..." className="mt-2 w-full resize-y rounded-xl border border-slate-300 bg-white p-3.5 text-sm leading-6 outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-500/15 dark:border-slate-700 dark:bg-slate-900" /><div className="mt-4 flex flex-wrap items-center gap-3"><Button onClick={() => void save()} isLoading={saving} disabled={!answer.trim()}><FiCheck />{saving ? 'Saving...' : 'Save Answer'}</Button>{question.answerText?.trim() && <Button variant="secondary" onClick={() => void askFollowUp()} isLoading={generatingFollowUp}><FiCpu />Ask Follow-up</Button>}{saved && <span role="status" className="text-sm font-medium text-emerald-600">Answer saved</span>}</div>{followUpMessage && <p role="status" className="mt-3 text-sm text-slate-600 dark:text-slate-300">{followUpMessage}</p>}</Card>
    <AnswerEvaluationPanel evaluation={evaluation} category={question.category} hasAnswer={Boolean(question.answerText?.trim())} loading={evaluationLoading} evaluating={evaluating} onEvaluate={() => void evaluate()} />
    <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-between"><Button variant="secondary" disabled={index === 0} onClick={() => setIndex(value => value - 1)}><FiChevronLeft />Previous</Button><div className="flex gap-3">{index === session.questions.length - 1 ? <Button onClick={() => void complete()} isLoading={completing}>Complete Interview</Button> : <Button variant="secondary" onClick={() => setIndex(value => value + 1)}>Next<FiChevronRight /></Button>}</div></div>
  </div>
}

interface CompletedProps { session: InterviewSessionType; index: number; setIndex: (value: number) => void; evaluation: InterviewAnswerEvaluation | null; evaluationLoading: boolean; evaluating: boolean; onEvaluate: () => void; error: string }
function CompletedSession({ session, index, setIndex, evaluation, evaluationLoading, evaluating, onEvaluate, error }: CompletedProps) {
  const question = session.questions[index]; const actualTotal = session.questions.length; const adaptiveTotal = session.questions.filter(value => value.adaptive).length; const unanswered = actualTotal - session.answeredQuestions
  const [summary, setSummary] = useState<InterviewSummary | null>(null); const [summaryLoading, setSummaryLoading] = useState(true); const [summaryError, setSummaryError] = useState('')
  useEffect(() => { let active = true; setSummaryLoading(true); analyticsService.getInterviewSummary(session.id).then(value => { if (active) { setSummary(value); setSummaryError('') } }).catch(requestError => { if (active) setSummaryError(getApiErrorMessage(requestError)) }).finally(() => { if (active) setSummaryLoading(false) }); return () => { active = false } }, [session.id, evaluation?.evaluatedAt])
  return <div className="mx-auto max-w-4xl space-y-6">
    <Link to="/history" className="inline-flex items-center gap-2 text-sm font-semibold text-indigo-600"><FiArrowLeft />Interview history</Link>
    {error && <Alert message={error} />}
    <Card className="p-6 sm:p-8"><span className="grid size-12 place-items-center rounded-full bg-emerald-50 text-emerald-600 dark:bg-emerald-950/40"><FiCheck /></span><h2 className="mt-5 text-3xl font-bold">Interview completed</h2><p className="mt-2 text-slate-500">Your answers have been saved for review and optional AI-generated feedback.</p><div className="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-4"><Summary label="Base questions" value={String(session.totalQuestions)} /><Summary label="Adaptive follow-ups" value={String(adaptiveTotal)} /><Summary label="Answered" value={`${session.answeredQuestions} / ${actualTotal}`} /><Summary label="Unanswered" value={String(unanswered)} /></div><div className="mt-5 flex flex-wrap gap-3"><Link to={`/interviews/${session.id}/replay`} className="inline-flex min-h-11 items-center rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white">Replay Interview</Link><Link to={`/interviews/${session.id}/coaching`} className="inline-flex min-h-11 items-center rounded-xl border border-slate-300 px-4 py-2.5 text-sm font-semibold dark:border-slate-700">Generate Coaching Plan</Link></div><p className="mt-5 text-sm text-slate-500">Completed {formatDate(session.completedAt)}</p></Card>
    {summaryLoading ? <LoadingSpinner label="Loading interview summary..." /> : summaryError ? <Alert message={summaryError} /> : summary && <section className="space-y-5" aria-labelledby="summary-heading"><Card className="p-5 sm:p-6"><h3 id="summary-heading" className="text-xl font-bold">Interview summary</h3><div className="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-4"><Summary label="Average answer score" value={summary.averageScore == null ? 'No data' : `${summary.averageScore.toFixed(1)} / 100`} /><Summary label="Evaluated answers" value={`${summary.evaluatedQuestions} / ${summary.totalQuestions}`} /><Summary label="Strongest measured category" value={summary.strongestCategory || 'Not enough data'} /><Summary label="Lowest measured category" value={summary.weakestCategory || 'Not enough data'} /></div><div className="mt-5"><ScoreBreakdown scores={summary} /></div></Card><div className="grid gap-5 lg:grid-cols-2"><CategoryPerformanceCard scores={summary.categoryScores} /><SkillPerformanceList scores={summary.skillScores} /></div></section>}
    <Card className="p-5 sm:p-6"><p className="text-sm text-slate-500">Question {index + 1} of {actualTotal}</p><div className="mt-3 flex flex-wrap gap-2">{question.adaptive && <Badge>ADAPTIVE FOLLOW-UP</Badge>}<Badge>{question.category}</Badge>{question.skill && <Badge>{question.skill}</Badge>}{question.focusArea && <Badge>{question.focusArea}</Badge>}</div><h3 className="mt-5 text-lg font-bold">{question.questionText}</h3><p className="mt-5 whitespace-pre-wrap rounded-xl bg-slate-50 p-4 text-sm leading-6 dark:bg-slate-800/60">{question.answerText || 'No answer was submitted.'}</p><div className="mt-5 flex justify-between"><Button variant="secondary" disabled={index === 0} onClick={() => setIndex(index - 1)}>Previous</Button><Button variant="secondary" disabled={index === session.questions.length - 1} onClick={() => setIndex(index + 1)}>Next</Button></div></Card>
    <AnswerEvaluationPanel evaluation={evaluation} category={question.category} hasAnswer={Boolean(question.answerText?.trim())} loading={evaluationLoading} evaluating={evaluating} onEvaluate={onEvaluate} />
  </div>
}

function Badge({ children }: { children: string }) { return <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold text-indigo-700 dark:bg-indigo-950/40 dark:text-indigo-300">{children}</span> }
function Summary({ label, value }: { label: string; value: string }) { return <div className="rounded-xl bg-slate-50 p-3 dark:bg-slate-800/60"><p className="text-xs text-slate-500">{label}</p><p className="mt-1 font-bold">{value}</p></div> }
