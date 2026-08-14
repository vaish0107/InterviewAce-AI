import type { CoachingFocusArea } from '../../types/coaching'
import { Card } from '../common/Card'

export function CoachingFocusAreaCard({ area }: { area: CoachingFocusArea }) {
  const relatedSkills = area.relatedSkills ?? []
  return <Card className="p-5">
    <p className="text-xs font-bold uppercase tracking-wide text-indigo-600">{area.priority || 'Unspecified'} Priority</p>
    <h3 className="mt-2 text-lg font-bold">{area.title || 'Focus area'}</h3>
    <p className="mt-2 text-sm leading-6 text-slate-600 dark:text-slate-300">{area.reason || 'No reason was provided.'}</p>
    <p className="mt-3 text-sm"><span className="font-semibold">Related skills:</span> {relatedSkills.length ? relatedSkills.join(', ') : 'No related skills listed.'}</p>
  </Card>
}
