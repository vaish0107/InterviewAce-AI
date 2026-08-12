export function LoadingSpinner({ label = 'Loading' }: { label?: string }) {
  return <div className="flex min-h-48 items-center justify-center gap-3 text-slate-600 dark:text-slate-300" role="status">
    <span className="size-6 animate-spin rounded-full border-2 border-indigo-600 border-r-transparent" aria-hidden="true" />
    <span>{label}</span>
  </div>
}
