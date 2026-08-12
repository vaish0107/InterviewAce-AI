import { useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { FiBriefcase, FiClock, FiFileText, FiHome, FiLogOut, FiMenu, FiMessageSquare, FiX } from 'react-icons/fi'
import { useAuth } from '../../context/AuthContext'
import { ThemeToggle } from '../common/ThemeToggle'

const links = [
  { to: '/dashboard', label: 'Dashboard', icon: FiHome },
  { to: '/resumes', label: 'My Resumes', icon: FiFileText },
  { to: '/job-match', label: 'Job Match', icon: FiBriefcase },
  { to: '/interviews', label: 'Interview Practice', icon: FiMessageSquare },
  { to: '/history', label: 'History', icon: FiClock },
]

export function AppLayout() {
  const [open, setOpen] = useState(false); const { user, logout } = useAuth(); const location = useLocation()
  const currentLabel = links.find(link => location.pathname.startsWith(link.to))?.label || 'InterviewAce AI'
  const sidebar = <aside className="flex h-full w-72 flex-col border-r border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-950">
    <div className="flex h-14 items-center justify-between px-2"><div className="flex items-center gap-3"><span className="grid size-10 place-items-center rounded-xl bg-indigo-600 text-sm font-bold text-white">IA</span><div><p className="font-bold text-slate-950 dark:text-white">InterviewAce AI</p><p className="text-xs text-slate-500">Career workspace</p></div></div><button onClick={() => setOpen(false)} className="grid size-10 place-items-center rounded-lg lg:hidden" aria-label="Close navigation"><FiX /></button></div>
    <nav className="mt-7 flex-1 space-y-1" aria-label="Main navigation">{links.map(({ to, label, icon: Icon }) => <NavLink key={to} to={to} onClick={() => setOpen(false)} className={({ isActive }) => `flex min-h-11 items-center gap-3 rounded-xl px-3 text-sm font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 ${isActive ? 'bg-indigo-50 text-indigo-700 dark:bg-indigo-500/10 dark:text-indigo-300' : 'text-slate-600 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-900'}`}><Icon aria-hidden="true" /><span>{label}</span></NavLink>)}</nav>
    <div className="border-t border-slate-200 pt-4 dark:border-slate-800"><div className="min-w-0 px-2"><p className="truncate text-sm font-semibold text-slate-900 dark:text-white">{user?.fullName}</p><p className="truncate text-xs text-slate-500">{user?.email}</p></div><button onClick={logout} className="mt-3 flex min-h-11 w-full items-center gap-3 rounded-xl px-3 text-sm font-medium text-slate-600 hover:bg-red-50 hover:text-red-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500 dark:text-slate-400 dark:hover:bg-red-950/30 dark:hover:text-red-300"><FiLogOut />Logout</button></div>
  </aside>
  return <div className="min-h-screen bg-slate-50 text-slate-900 dark:bg-slate-950 dark:text-white">
    <div className="fixed inset-y-0 left-0 z-40 hidden lg:block">{sidebar}</div>
    {open && <div className="fixed inset-0 z-50 lg:hidden"><button className="absolute inset-0 bg-slate-950/55" onClick={() => setOpen(false)} aria-label="Close navigation overlay" /> <div className="relative h-full w-72">{sidebar}</div></div>}
    <div className="lg:pl-72"><header className="sticky top-0 z-30 flex h-18 items-center justify-between border-b border-slate-200 bg-white/90 px-4 backdrop-blur sm:px-7 dark:border-slate-800 dark:bg-slate-950/90"><div className="flex items-center gap-3"><button onClick={() => setOpen(true)} className="grid size-10 place-items-center rounded-xl hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 lg:hidden" aria-label="Open navigation"><FiMenu /></button><h1 className="font-semibold">{currentLabel}</h1></div><ThemeToggle /></header><main className="mx-auto max-w-7xl p-4 sm:p-7 lg:p-9"><Outlet /></main></div>
  </div>
}
