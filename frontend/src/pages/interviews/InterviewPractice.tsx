import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { FiEdit3, FiMic, FiPlay, FiUser, FiUsers, FiVolume2 } from 'react-icons/fi'
import type { Resume } from '../../types/resume'
import type { InterviewDifficulty, InterviewSession, InterviewType } from '../../types/interview'
import { resumeService } from '../../services/resumeService'
import { interviewService } from '../../services/interviewService'
import { getApiErrorMessage } from '../../utils/getApiErrorMessage'
import { Alert } from '../../components/common/Alert'
import { Button } from '../../components/common/Button'
import { Card } from '../../components/common/Card'
import { LoadingSpinner } from '../../components/common/LoadingSpinner'
import { InterviewHistoryList } from '../../components/interview/InterviewHistoryList'

const types: { value: InterviewType; title: string; text: string; icon: typeof FiUser }[] = [
  { value: 'TECHNICAL', title: 'Technical', text: 'Skill-based and project questions', icon: FiUser },
  { value: 'HR', title: 'HR', text: 'Behavioral and workplace questions', icon: FiUsers },
  { value: 'MIXED', title: 'Mixed', text: 'Technical, HR, and project questions', icon: FiPlay },
]

export function InterviewPractice() {
  const [resumes, setResumes] = useState<Resume[]>([]); const [sessions, setSessions] = useState<InterviewSession[]>([])
  const [loading, setLoading] = useState(true); const [error, setError] = useState(''); const [starting, setStarting] = useState(false)
  const [type, setType] = useState<InterviewType>('MIXED'); const [difficulty, setDifficulty] = useState<InterviewDifficulty>('MEDIUM')
  const [mode, setMode] = useState<'text' | 'voice'>('text')
  const [count, setCount] = useState(10); const [resumeId, setResumeId] = useState<number | null>(null); const navigate = useNavigate()
  useEffect(() => {
    let active = true
    Promise.all([resumeService.getResumes(), interviewService.getInterviews()]).then(([resumeValues, sessionValues]) => {
      if (!active) return
      setResumes(resumeValues); setSessions(sessionValues)
      const analyzed = resumeValues.find(value => value.uploadStatus === 'COMPLETED'); if (analyzed) setResumeId(analyzed.id)
    }).catch(requestError => { if (active) setError(getApiErrorMessage(requestError)) }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])
  const analyzedResumes = resumes.filter(value => value.uploadStatus === 'COMPLETED')
  const requiresResume = type !== 'HR'
  const start = async () => {
    if (requiresResume && !resumeId) { setError('Select a resume with completed ATS analysis.'); return }
    setStarting(true); setError('')
    try { const session = await interviewService.createInterview({ resumeId: type === 'HR' ? resumeId : resumeId!, interviewType: type, difficulty, questionCount: count }); navigate(`/interviews/${session.id}${mode === 'voice' ? '/voice' : ''}`) }
    catch (requestError) { setError(getApiErrorMessage(requestError)) }
    finally { setStarting(false) }
  }
  if (loading) return <LoadingSpinner label="Loading interview practice…" />
  return <div className="space-y-9"><div><p className="text-sm font-semibold uppercase tracking-wide text-indigo-600 dark:text-indigo-400">Practice without pressure</p><h2 className="mt-2 text-3xl font-bold tracking-tight">Interview Practice</h2><p className="mt-2 text-slate-500 dark:text-slate-400">Build a deterministic question set from your resume skills. Answers are saved but not scored.</p></div>
    {error && <Alert message={error} />}
    <Card className="p-5 sm:p-7"><h3 className="text-xl font-bold">Create a session</h3><fieldset className="mt-6"><legend className="text-sm font-medium">Interview mode</legend><div className="mt-2 grid gap-3 sm:grid-cols-2"><button type="button" onClick={() => setMode('text')} aria-pressed={mode === 'text'} className={`rounded-xl border p-4 text-left transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 ${mode === 'text' ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-950/30' : 'border-slate-200 hover:border-indigo-300 dark:border-slate-800'}`}><FiEdit3 className="text-indigo-600" /><p className="mt-3 font-bold">Text Interview</p><p className="mt-1 text-xs leading-5 text-slate-500">Type answers in the existing workflow. Best for careful practice.</p></button><button type="button" onClick={() => setMode('voice')} aria-pressed={mode === 'voice'} className={`rounded-xl border p-4 text-left transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 ${mode === 'voice' ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-950/30' : 'border-slate-200 hover:border-indigo-300 dark:border-slate-800'}`}><FiMic className="text-indigo-600" /><p className="mt-3 font-bold">Voice Interview</p><p className="mt-1 text-xs leading-5 text-slate-500">Speak answers while questions are read aloud for a realistic simulation.</p></button></div>{mode === 'voice' && <p className="mt-3 flex items-start gap-2 rounded-xl bg-indigo-50 p-3 text-sm text-indigo-800 dark:bg-indigo-950/30 dark:text-indigo-200"><FiVolume2 className="mt-0.5 shrink-0" />Questions will be read aloud and you can answer using your microphone. Your speech will be converted to text before saving.</p>}</fieldset><div className="mt-6"><span className="text-sm font-medium">Interview type</span><div className="mt-2 grid gap-3 sm:grid-cols-3">{types.map(item => <button key={item.value} type="button" onClick={() => setType(item.value)} aria-pressed={type === item.value} className={`rounded-xl border p-4 text-left transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 ${type === item.value ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-950/30' : 'border-slate-200 hover:border-indigo-300 dark:border-slate-800'}`}><item.icon className="text-indigo-600" /><p className="mt-3 font-bold">{item.title}</p><p className="mt-1 text-xs leading-5 text-slate-500">{item.text}</p></button>)}</div></div>
      <div className="mt-6 grid gap-5 md:grid-cols-3"><div><label htmlFor="interview-resume" className="text-sm font-medium">Resume {requiresResume ? '' : '(optional)'}</label><select id="interview-resume" value={resumeId ?? ''} onChange={event => setResumeId(event.target.value ? Number(event.target.value) : null)} className="mt-1.5 min-h-11 w-full rounded-xl border border-slate-300 bg-white px-3 dark:border-slate-700 dark:bg-slate-900"><option value="">{requiresResume ? 'Select analyzed resume' : 'No resume'}</option>{analyzedResumes.map(resume => <option key={resume.id} value={resume.id}>{resume.originalFileName}</option>)}</select>{requiresResume && !analyzedResumes.length && <p className="mt-2 text-xs text-amber-700 dark:text-amber-300">Analyze a resume first. <Link className="font-semibold underline" to="/resumes">Open resumes</Link></p>}</div>
        <div><label htmlFor="interview-difficulty" className="text-sm font-medium">Difficulty</label><select id="interview-difficulty" value={difficulty} onChange={event => setDifficulty(event.target.value as InterviewDifficulty)} className="mt-1.5 min-h-11 w-full rounded-xl border border-slate-300 bg-white px-3 dark:border-slate-700 dark:bg-slate-900"><option>EASY</option><option>MEDIUM</option><option>HARD</option></select></div>
        <div><label htmlFor="question-count" className="text-sm font-medium">Questions</label><select id="question-count" value={count} onChange={event => setCount(Number(event.target.value))} className="mt-1.5 min-h-11 w-full rounded-xl border border-slate-300 bg-white px-3 dark:border-slate-700 dark:bg-slate-900">{[3, 5, 8, 10, 12, 15, 20].map(value => <option key={value}>{value}</option>)}</select></div></div>
      <Button onClick={() => void start()} isLoading={starting} disabled={starting || (requiresResume && !resumeId)} className="mt-6"><FiPlay />{starting ? 'Generating questions…' : `Start ${mode === 'voice' ? 'Voice' : 'Text'} Interview`}</Button>
    </Card>
    <section><div className="mb-4 flex items-end justify-between"><div><h3 className="text-xl font-bold">Recent sessions</h3><p className="mt-1 text-sm text-slate-500">Continue where you left off or review completed answers.</p></div><Link to="/history" className="text-sm font-semibold text-indigo-600 dark:text-indigo-400">View all</Link></div><InterviewHistoryList sessions={sessions.slice(0, 4)} compact /></section>
  </div>
}
