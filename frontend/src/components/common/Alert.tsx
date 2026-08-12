import { FiAlertCircle } from 'react-icons/fi'
export function Alert({ message }: { message: string }) {
  return <div role="alert" className="flex gap-3 rounded-xl border border-red-200 bg-red-50 p-3.5 text-sm text-red-700 dark:border-red-900/60 dark:bg-red-950/40 dark:text-red-300">
    <FiAlertCircle className="mt-0.5 shrink-0" aria-hidden="true" /><span>{message}</span>
  </div>
}
