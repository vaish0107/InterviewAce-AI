import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './components/auth/ProtectedRoute'
import { AppLayout } from './components/layout/AppLayout'
import { LoadingSpinner } from './components/common/LoadingSpinner'
import { useAuth } from './context/AuthContext'
import { Login } from './pages/auth/Login'
import { Register } from './pages/auth/Register'
import { Dashboard } from './pages/dashboard/Dashboard'
import { Resumes } from './pages/resumes/Resumes'
import { ResumeAnalysis } from './pages/resumes/ResumeAnalysis'
import { JobMatch } from './pages/jobMatch/JobMatch'
import { InterviewPractice } from './pages/interviews/InterviewPractice'
import { InterviewSession } from './pages/interviews/InterviewSession'
import { VoiceInterviewSession } from './pages/interviews/VoiceInterviewSession'
import { InterviewHistory } from './pages/interviews/InterviewHistory'
import { NotFound } from './pages/NotFound'

function HomeRedirect() {
  const { isAuthenticated, isLoading } = useAuth()
  if (isLoading) return <LoadingSpinner label="Loading InterviewAce…" />
  return <Navigate to={isAuthenticated ? '/dashboard' : '/login'} replace />
}

export default function App() {
  return <Routes>
    <Route path="/" element={<HomeRedirect />} />
    <Route path="/login" element={<Login />} />
    <Route path="/register" element={<Register />} />
    <Route element={<ProtectedRoute />}>
      <Route element={<AppLayout />}>
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/resumes" element={<Resumes />} />
        <Route path="/resumes/:id/analysis" element={<ResumeAnalysis />} />
        <Route path="/resume-analysis" element={<Navigate to="/resumes" replace />} />
        <Route path="/job-match" element={<JobMatch />} />
        <Route path="/interviews" element={<InterviewPractice />} />
        <Route path="/interviews/:id" element={<InterviewSession />} />
        <Route path="/interviews/:id/voice" element={<VoiceInterviewSession />} />
        <Route path="/history" element={<InterviewHistory />} />
      </Route>
    </Route>
    <Route path="*" element={<NotFound />} />
  </Routes>
}
