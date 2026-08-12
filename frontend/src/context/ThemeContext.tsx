/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'

type Theme = 'light' | 'dark'
interface ThemeContextValue { theme: Theme; toggleTheme: () => void }
const ThemeContext = createContext<ThemeContextValue | undefined>(undefined)

function initialTheme(): Theme {
  const saved = localStorage.getItem('interviewace_theme')
  if (saved === 'light' || saved === 'dark') return saved
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<Theme>(initialTheme)
  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark')
    localStorage.setItem('interviewace_theme', theme)
  }, [theme])
  return <ThemeContext.Provider value={{ theme, toggleTheme: () => setTheme(value => value === 'dark' ? 'light' : 'dark') }}>{children}</ThemeContext.Provider>
}

export function useTheme() {
  const context = useContext(ThemeContext)
  if (!context) throw new Error('useTheme must be used inside ThemeProvider')
  return context
}
