import type { ReactNode } from 'react'
import { FiCheckCircle } from 'react-icons/fi'
import { ThemeToggle } from '../common/ThemeToggle'

export function AuthShell({ children, title, subtitle }: { children: ReactNode; title: string; subtitle: string }) {
  return <main className="grid min-h-screen bg-slate-50 dark:bg-slate-950 lg:grid-cols-[1.05fr_.95fr]">
    <section className="relative hidden overflow-hidden bg-slate-950 p-12 text-white lg:flex lg:flex-col lg:justify-between">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(79,70,229,.28),transparent_38%)]" />
      <div className="relative flex items-center gap-3"><span className="grid size-11 place-items-center rounded-xl bg-indigo-600 text-lg font-bold">IA</span><span className="text-xl font-bold">InterviewAce AI</span></div>
      <div className="relative max-w-lg">
        <p className="text-sm font-semibold uppercase tracking-[.2em] text-indigo-300">Prepare with purpose</p>
        <h2 className="mt-5 text-4xl font-bold leading-tight">Turn every application into a stronger interview.</h2>
        <div className="mt-8 space-y-4 text-slate-300">
          {['Understand your resume signal', 'Match your skills to real roles', 'Build confidence through practice'].map(item => <div key={item} className="flex items-center gap-3"><FiCheckCircle className="text-indigo-400" /><span>{item}</span></div>)}
        </div>
      </div>
      <p className="relative text-sm text-slate-500">Your career preparation workspace.</p>
    </section>
    <section className="flex min-h-screen flex-col p-5 sm:p-8">
      <div className="flex justify-between lg:justify-end"><div className="flex items-center gap-2 font-bold text-slate-900 dark:text-white lg:hidden"><span className="grid size-9 place-items-center rounded-lg bg-indigo-600 text-xs text-white">IA</span>InterviewAce AI</div><ThemeToggle /></div>
      <div className="mx-auto flex w-full max-w-md flex-1 flex-col justify-center py-10">
        <h1 className="text-3xl font-bold tracking-tight text-slate-950 dark:text-white">{title}</h1>
        <p className="mt-2 text-slate-500 dark:text-slate-400">{subtitle}</p>
        <div className="mt-8">{children}</div>
      </div>
    </section>
  </main>
}
