import { EmptyState } from '../components/common/EmptyState'
export function PlaceholderPage({ title }: { title: string }) {
  return <div><h2 className="text-3xl font-bold tracking-tight">{title}</h2><p className="mt-2 text-slate-500 dark:text-slate-400">This workspace is ready for the next feature.</p><div className="mt-8"><EmptyState title="Coming in the next development phase" description={`${title} functionality will be added without changing your current account experience.`} /></div></div>
}
