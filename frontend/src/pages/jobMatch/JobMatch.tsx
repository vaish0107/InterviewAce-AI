import { useEffect, useState, type ReactNode } from 'react'
import axios from 'axios'
import { Link } from 'react-router-dom'
import { FiBriefcase, FiCheckCircle, FiTarget, FiTrendingUp } from 'react-icons/fi'
import type { Resume } from '../../types/resume'
import type { JobMatchAnalysis } from '../../types/jobMatch'
import { resumeService } from '../../services/resumeService'
import { jobMatchService } from '../../services/jobMatchService'
import { formatDate } from '../../utils/formatters'
import { getApiErrorMessage } from '../../utils/getApiErrorMessage'
import { Alert } from '../../components/common/Alert'
import { Button } from '../../components/common/Button'
import { Card } from '../../components/common/Card'
import { EmptyState } from '../../components/common/EmptyState'
import { LoadingSpinner } from '../../components/common/LoadingSpinner'
import { JobMatchSummary } from '../../components/jobMatch/JobMatchSummary'
import { CategoryMatchCard } from '../../components/jobMatch/CategoryMatchCard'
import { JobMatchHistory } from '../../components/jobMatch/JobMatchHistory'

export function JobMatch() {
  const [resumes, setResumes] = useState<Resume[]>([])
  const [resumesLoading, setResumesLoading] = useState(true)
  const [resumeError, setResumeError] = useState('')
  const [selectedResumeId, setSelectedResumeId] = useState<number | null>(null)
  const [description, setDescription] = useState('')
  const [validationError, setValidationError] = useState('')
  const [result, setResult] = useState<JobMatchAnalysis | null>(null)
  const [history, setHistory] = useState<JobMatchAnalysis[]>([])
  const [latestLoading, setLatestLoading] = useState(false)
  const [historyLoading, setHistoryLoading] = useState(false)
  const [resultError, setResultError] = useState('')
  const [analyzing, setAnalyzing] = useState(false)

  useEffect(() => {
    let active = true
    resumeService.getResumes().then(values => {
      if (!active) return
      setResumes(values)
      const selectable = values.find(item => item.uploadStatus !== 'PROCESSING')
      if (selectable) setSelectedResumeId(selectable.id)
    }).catch(error => { if (active) setResumeError(getApiErrorMessage(error)) })
      .finally(() => { if (active) setResumesLoading(false) })
    return () => { active = false }
  }, [])

  useEffect(() => {
    if (!selectedResumeId) { setResult(null); setHistory([]); return }
    let active = true
    setLatestLoading(true); setHistoryLoading(true); setResultError(''); setResult(null); setHistory([])
    const latest = jobMatchService.getLatestJobMatch(selectedResumeId)
      .then(value => { if (active) setResult(value) })
      .catch(error => { if (active && !(axios.isAxiosError(error) && error.response?.status === 404)) setResultError(getApiErrorMessage(error)) })
      .finally(() => { if (active) setLatestLoading(false) })
    const previous = jobMatchService.getJobMatchHistory(selectedResumeId)
      .then(values => { if (active) setHistory(values) })
      .catch(error => { if (active) setResultError(getApiErrorMessage(error)) })
      .finally(() => { if (active) setHistoryLoading(false) })
    void Promise.allSettled([latest, previous])
    return () => { active = false }
  }, [selectedResumeId])

  const analyze = async () => {
    if (!selectedResumeId) { setValidationError('Select a resume before running job matching.'); return }
    if (!description.trim()) { setValidationError('Job description is required.'); return }
    if (description.trim().length < 20) { setValidationError('Job description must contain at least 20 characters.'); return }
    setValidationError(''); setResultError(''); setAnalyzing(true)
    try {
      const analysis = await jobMatchService.analyzeJobMatch(selectedResumeId, description)
      setResult(analysis)
      setHistory(await jobMatchService.getJobMatchHistory(selectedResumeId))
    } catch (error) { setResultError(getApiErrorMessage(error)) }
    finally { setAnalyzing(false) }
  }

  const validResumes = resumes.filter(item => item.uploadStatus !== 'PROCESSING')
  return <div className="space-y-8">
    <div><p className="text-sm font-semibold uppercase tracking-wide text-indigo-600 dark:text-indigo-400">Skill alignment</p><h2 className="mt-2 text-3xl font-bold tracking-tight">Job Match</h2><p className="mt-2 text-slate-500 dark:text-slate-400">Compare your resume with a job description and identify skill gaps.</p></div>
    {resumesLoading ? <LoadingSpinner label="Loading your resumes…" /> : resumeError ? <Alert message={resumeError} /> : resumes.length === 0 ? <EmptyState title="Upload a resume before running job matching." description="InterviewAce needs a stored PDF to detect and compare your skills." action={<LinkButton to="/resumes">Upload a resume</LinkButton>} /> : <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
      <div className="space-y-6">
        <Card className="p-5 sm:p-6"><div className="flex items-center gap-3"><span className="grid size-10 place-items-center rounded-xl bg-indigo-50 text-indigo-600 dark:bg-indigo-950/40 dark:text-indigo-300"><FiBriefcase /></span><div><h3 className="font-bold">Compare with a role</h3><p className="text-sm text-slate-500">Choose a resume and paste the complete job description.</p></div></div>
          <div className="mt-6 space-y-5">
            <div><label htmlFor="match-resume" className="block text-sm font-medium text-slate-700 dark:text-slate-200">Resume</label><select id="match-resume" value={selectedResumeId ?? ''} onChange={event => setSelectedResumeId(Number(event.target.value))} className="mt-1.5 min-h-11 w-full rounded-xl border border-slate-300 bg-white px-3.5 text-slate-900 outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-500/15 dark:border-slate-700 dark:bg-slate-900 dark:text-white"><option value="" disabled>Select a resume</option>{validResumes.map(item => <option key={item.id} value={item.id}>{item.originalFileName} · {item.uploadStatus} · {formatDate(item.uploadedAt)}</option>)}</select>{validResumes.length === 0 && <p className="mt-2 text-sm text-amber-700 dark:text-amber-300">Your resumes are currently processing. Try again shortly.</p>}</div>
            <div><div className="flex items-center justify-between gap-3"><label htmlFor="job-description" className="text-sm font-medium text-slate-700 dark:text-slate-200">Job Description</label><span className="text-xs text-slate-500">{description.length} characters</span></div><textarea id="job-description" value={description} onChange={event => { setDescription(event.target.value); if (validationError) setValidationError('') }} rows={12} placeholder="Paste the complete job description here..." aria-invalid={Boolean(validationError)} aria-describedby={validationError ? 'job-description-error' : 'job-description-help'} className={`mt-1.5 w-full resize-y rounded-xl border bg-white px-3.5 py-3 text-sm leading-6 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-3 focus:ring-indigo-500/15 dark:bg-slate-900 dark:text-white ${validationError ? 'border-red-500' : 'border-slate-300 dark:border-slate-700'}`} /><p id="job-description-help" className="mt-1.5 text-xs text-slate-500">The description is sent exactly as entered and must contain at least 20 characters.</p>{validationError && <p id="job-description-error" className="mt-1.5 text-sm text-red-600 dark:text-red-400">{validationError}</p>}</div>
            <Button type="button" onClick={() => void analyze()} isLoading={analyzing} disabled={!selectedResumeId || analyzing} className="w-full sm:w-auto">{analyzing ? 'Comparing resume with job requirements…' : 'Analyze Match'}</Button>
          </div>
        </Card>
        {resultError && <Alert message={resultError} />}
        {latestLoading ? <LoadingSpinner label="Loading latest match…" /> : !result && !resultError ? <EmptyState title="No job match analysis exists for this resume yet." description="Paste a job description above to create the first comparison." /> : result && <JobMatchResult analysis={result} />}
      </div>
      <aside><JobMatchHistory history={history} selectedId={result?.id} loading={historyLoading} onSelect={setResult} /></aside>
    </div>}
  </div>
}

function JobMatchResult({ analysis }: { analysis: JobMatchAnalysis }) {
  const categories = Object.entries(analysis.categoryMatches)
  return <div className="space-y-6"><JobMatchSummary analysis={analysis} />
    <div className="grid gap-5 lg:grid-cols-3"><SkillSection title="Matched Skills" description="Required skills detected in your resume." skills={analysis.matchedSkills} empty="No required skills were matched." tone="matched" icon={<FiCheckCircle />} /><SkillSection title="Missing Skills" description="Skills mentioned in the job description but not detected in your resume." skills={analysis.missingSkills} empty="No required skills appear to be missing." tone="missing" icon={<FiTarget />} /><SkillSection title="Additional Resume Skills" description="Skills detected in your resume that were not explicitly required by this job description." skills={analysis.additionalResumeSkills} empty="No additional resume skills were detected." tone="additional" icon={<FiTrendingUp />} /></div>
    {categories.length > 0 && <section><h3 className="text-xl font-bold">Category breakdown</h3><p className="mt-1 text-sm text-slate-500">Skill alignment grouped by the backend analysis categories.</p><div className="mt-4 grid gap-4 lg:grid-cols-2">{categories.map(([name, category]) => <CategoryMatchCard key={name} name={name} category={category} />)}</div></section>}
    <div className="grid gap-5 lg:grid-cols-2"><TextList title="Strengths" items={analysis.strengths} empty="No specific strengths were generated for this match." /><TextList title="Recommendations" items={analysis.recommendations} empty="No recommendations were generated for this match." numbered /></div>
  </div>
}

function SkillSection({ title, description, skills, empty, tone, icon }: { title: string; description: string; skills: string[]; empty: string; tone: 'matched' | 'missing' | 'additional'; icon: ReactNode }) {
  const styles = { matched: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300', missing: 'bg-amber-50 text-amber-800 dark:bg-amber-950/40 dark:text-amber-300', additional: 'bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300' }
  return <Card className="p-5"><div className="flex items-center gap-2 font-bold"><span className={tone === 'matched' ? 'text-emerald-600' : tone === 'missing' ? 'text-amber-600' : 'text-blue-600'}>{icon}</span><h3>{title}</h3></div><p className="mt-2 text-xs leading-5 text-slate-500">{description}</p>{skills.length ? <div className="mt-4 flex flex-wrap gap-2">{skills.map(skill => <span key={skill} className={`rounded-full px-2.5 py-1 text-xs font-medium ${styles[tone]}`}>{skill}</span>)}</div> : <p className="mt-4 text-sm text-slate-500">{empty}</p>}</Card>
}

function TextList({ title, items, empty, numbered = false }: { title: string; items: string[]; empty: string; numbered?: boolean }) {
  return <Card className="p-5"><h3 className="text-lg font-bold">{title}</h3>{items.length ? <ul className="mt-4 space-y-3">{items.map((item, index) => <li key={`${index}-${item}`} className="flex gap-3 rounded-xl bg-slate-50 p-3 text-sm leading-6 dark:bg-slate-800/60"><span className="font-bold text-indigo-600">{numbered ? `${index + 1}.` : '✓'}</span><span>{item}</span></li>)}</ul> : <p className="mt-4 text-sm text-slate-500">{empty}</p>}</Card>
}

function LinkButton({ to, children }: { to: string; children: ReactNode }) {
  return <Link to={to} className="inline-flex min-h-11 items-center rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500">{children}</Link>
}
