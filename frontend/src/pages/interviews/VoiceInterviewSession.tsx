import axios from 'axios'
import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { FiArrowLeft, FiCheck, FiChevronLeft, FiChevronRight, FiVolume2, FiVolumeX } from 'react-icons/fi'
import type { InterviewAnswerEvaluation, InterviewSession } from '../../types/interview'
import { interviewService } from '../../services/interviewService'
import { getApiErrorMessage } from '../../utils/getApiErrorMessage'
import { useSpeechRecognition } from '../../hooks/useSpeechRecognition'
import { useSpeechSynthesis } from '../../hooks/useSpeechSynthesis'
import { Alert } from '../../components/common/Alert'
import { Button } from '../../components/common/Button'
import { Card } from '../../components/common/Card'
import { LoadingSpinner } from '../../components/common/LoadingSpinner'
import { AnswerEvaluationPanel } from '../../components/interview/AnswerEvaluationPanel'
import { TranscriptEditor } from '../../components/interview/TranscriptEditor'
import { VoiceRecorderControls } from '../../components/interview/VoiceRecorderControls'

export function VoiceInterviewSession() {
  const sessionId = Number(useParams().id)
  const navigate = useNavigate()
  const [session, setSession] = useState<InterviewSession | null>(null)
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
  const [autoRead, setAutoRead] = useState(() => localStorage.getItem('interviewace:autoReadQuestions') !== 'false')
  const autoReadQuestionRef = useRef<number | null>(null)
  const recognition = useSpeechRecognition()
  const synthesis = useSpeechSynthesis()

  useEffect(() => {
    let active = true
    interviewService.getInterview(sessionId).then(value => { if (active) setSession(value) }).catch(requestError => { if (active) setError(getApiErrorMessage(requestError)) }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [sessionId])

  const question = session?.questions[index]
  const readOnly = session?.status === 'COMPLETED'
  useEffect(() => {
    const existing = question?.answerText || ''
    setAnswer(existing)
    recognition.setTranscript(existing)
    recognition.stopListening()
    synthesis.stop()
    setSaved(false)
  // Hook controls are stable; changing question is the intended trigger.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [question?.id, question?.answerText])

  useEffect(() => {
    if (recognition.transcript !== answer) { setAnswer(recognition.transcript); setSaved(false) }
  // Answer is deliberately omitted: edits are pushed into recognition state by the textarea.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [recognition.transcript])

  useEffect(() => {
    if (autoRead && question && autoReadQuestionRef.current !== question.id) {
      autoReadQuestionRef.current = question.id
      synthesis.speak(question.questionText)
    }
  }, [autoRead, question, synthesis])

  useEffect(() => {
    let active = true
    setEvaluation(null)
    if (!question?.id || !question.answerText?.trim()) return () => { active = false }
    setEvaluationLoading(true)
    interviewService.getAnswerEvaluation(sessionId, question.id).then(value => { if (active) setEvaluation(value) }).catch(requestError => {
      if (active && !(axios.isAxiosError(requestError) && requestError.response?.status === 404)) setError(getApiErrorMessage(requestError))
    }).finally(() => { if (active) setEvaluationLoading(false) })
    return () => { active = false }
  }, [question?.id, question?.answerText, sessionId])

  const changeAnswer = (value: string) => { setAnswer(value); recognition.setTranscript(value); setSaved(false) }
  const save = async () => {
    if (!session || !question || !answer.trim()) { setError('Review your transcript and enter an answer before saving.'); return }
    setSaving(true); setError(''); setSaved(false)
    try { const updated = await interviewService.submitAnswer(session.id, question.id, answer.trim()); setSession(updated); setEvaluation(null); setSaved(true) }
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
  const moveTo = useCallback((nextIndex: number) => {
    const hasUnsavedChanges = answer.trim() !== (question?.answerText || '').trim()
    if (hasUnsavedChanges && !window.confirm('Discard the unsaved transcript and change questions?')) return
    recognition.stopListening(); synthesis.stop(); recognition.clearTranscript(); setIndex(nextIndex)
  }, [answer, question?.answerText, recognition, synthesis])
  const setAutoReadPreference = (enabled: boolean) => { setAutoRead(enabled); localStorage.setItem('interviewace:autoReadQuestions', String(enabled)); if (!enabled) synthesis.stop() }

  if (loading) return <LoadingSpinner label="Loading voice interview..." />
  if (!session || !question) return <div><Alert message={error || 'Interview session has no questions.'} /><Link to="/interviews" className="mt-4 inline-flex text-sm font-semibold text-indigo-600">Back to interviews</Link></div>
  const progress = Math.round((session.answeredQuestions / session.totalQuestions) * 100)

  return <div className="mx-auto max-w-4xl space-y-6">
    <div><Link to="/interviews" className="inline-flex items-center gap-2 text-sm font-semibold text-indigo-600 dark:text-indigo-400"><FiArrowLeft />Back to interviews</Link><div className="mt-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between"><div><p className="text-sm text-slate-500">Question {index + 1} of {session.totalQuestions}</p><h2 className="mt-1 text-2xl font-bold">{session.interviewType} Voice Interview</h2><p className="mt-1 text-sm text-slate-500">{session.difficulty} difficulty</p></div><p className="text-sm font-semibold">Answered {session.answeredQuestions} / {session.totalQuestions}</p></div><div className="mt-4 h-2 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-800"><div className="h-full rounded-full bg-indigo-600 transition-all" style={{ width: `${progress}%` }} /></div></div>
    {error && <Alert message={error} />}{recognition.error && <Alert message={recognition.error} />}
    {!recognition.supported && <Card className="p-5"><p className="font-semibold">Voice recognition is not supported in this browser. You can continue using Text Interview mode.</p><Button className="mt-4" onClick={() => navigate(`/interviews/${session.id}`)}>Switch to Text Interview</Button></Card>}
    <Card className="p-5 sm:p-7"><div className="flex flex-wrap items-center justify-between gap-3"><div className="flex flex-wrap gap-2"><Badge>{question.category}</Badge>{question.skill && <Badge>{question.skill}</Badge>}<Badge>{question.difficulty}</Badge></div><label className="inline-flex min-h-11 cursor-pointer items-center gap-2 text-sm font-medium"><input type="checkbox" checked={autoRead} onChange={event => setAutoReadPreference(event.target.checked)} className="size-4 accent-indigo-600" />Auto-read questions</label></div><h3 className="mt-6 text-xl font-bold leading-8">{question.questionText}</h3><div className="mt-5 flex flex-wrap gap-3"><Button variant="secondary" onClick={() => synthesis.speak(question.questionText)} aria-label="Replay current question"><FiVolume2 />Replay Question</Button>{synthesis.speaking && <Button variant="ghost" onClick={synthesis.stop} aria-label="Stop reading question"><FiVolumeX />Stop speaking</Button>}</div></Card>
    <Card className="space-y-6 p-5 sm:p-7">
      {!readOnly && <VoiceRecorderControls listening={recognition.listening} hasTranscript={Boolean(answer.trim())} disabled={!recognition.supported} onStart={() => { synthesis.stop(); void recognition.startListening() }} onStop={recognition.stopListening} onRecordAgain={() => { recognition.clearTranscript(); setAnswer(''); setSaved(false); void recognition.startListening() }} />}
      <TranscriptEditor value={answer} interimTranscript={recognition.interimTranscript} listening={recognition.listening} readOnly={readOnly} onChange={changeAnswer} />
      {!readOnly && <div className="flex flex-wrap items-center gap-3"><Button onClick={() => void save()} isLoading={saving} disabled={!answer.trim() || recognition.listening}><FiCheck />{saving ? 'Saving...' : 'Save Answer'}</Button>{saved && <span role="status" className="text-sm font-medium text-emerald-600">Answer saved</span>}</div>}
    </Card>
    <p className="rounded-xl bg-slate-100 p-3 text-xs leading-5 text-slate-600 dark:bg-slate-800 dark:text-slate-300">Your microphone is used only while recording an answer. InterviewAce stores the transcript as your interview answer; this version does not upload or store raw microphone audio.</p>
    <AnswerEvaluationPanel evaluation={evaluation} category={question.category} hasAnswer={Boolean(question.answerText?.trim())} loading={evaluationLoading} evaluating={evaluating} onEvaluate={() => void evaluate()} />
    <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-between"><Button variant="secondary" disabled={index === 0} onClick={() => moveTo(index - 1)}><FiChevronLeft />Previous</Button><div className="flex gap-3">{!readOnly && index === session.questions.length - 1 ? <Button onClick={() => void complete()} isLoading={completing}>Complete Interview</Button> : <Button variant="secondary" disabled={index === session.questions.length - 1} onClick={() => moveTo(index + 1)}>Next<FiChevronRight /></Button>}</div></div>
  </div>
}

function Badge({ children }: { children: string }) { return <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-semibold text-indigo-700 dark:bg-indigo-950/40 dark:text-indigo-300">{children}</span> }
