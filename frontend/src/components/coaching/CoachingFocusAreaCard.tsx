import type { CoachingFocusArea } from '../../types/coaching'
import { Card } from '../common/Card'
import { Link, useParams } from 'react-router-dom'

export function CoachingFocusAreaCard({ area }: { area: CoachingFocusArea }) {
  const sourceInterviewId = Number(useParams().id)
  const relatedSkills = area.relatedSkills ?? []
  return <Card className="p-5">
    <p className="text-xs font-bold uppercase tracking-wide text-indigo-600">{area.priority || 'Unspecified'} Priority</p>
    <h3 className="mt-2 text-lg font-bold">{area.title || 'Focus area'}</h3>
    <p className="mt-2 text-sm leading-6 text-slate-600 dark:text-slate-300">{area.reason || 'No reason was provided.'}</p>
    <p className="mt-3 text-sm"><span className="font-semibold">Related skills:</span> {relatedSkills.length ? relatedSkills.join(', ') : 'No related skills listed.'}</p>
    <Link className="mt-4 inline-flex min-h-11 items-center rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white" to="/practice/targeted" state={{ sourceInterviewId, focusArea: area.title, skill: relatedSkills[0] ?? null, weaknessContext: area.reason }}>Practice This Area</Link>
  </Card>
}
