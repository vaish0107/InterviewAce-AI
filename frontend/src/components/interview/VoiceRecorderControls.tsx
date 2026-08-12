import { FiMic, FiRefreshCw, FiSquare } from 'react-icons/fi'
import { Button } from '../common/Button'

interface Props {
  listening: boolean
  hasTranscript: boolean
  disabled?: boolean
  onStart: () => void
  onStop: () => void
  onRecordAgain: () => void
}

export function VoiceRecorderControls({ listening, hasTranscript, disabled, onStart, onStop, onRecordAgain }: Props) {
  return <div>
    <div className="flex flex-wrap gap-3">
      {listening
        ? <Button onClick={onStop} aria-label="Stop recording answer"><FiSquare />Stop Answer</Button>
        : <Button onClick={onStart} disabled={disabled} aria-label={hasTranscript ? 'Continue recording answer' : 'Start recording answer'}><FiMic />{hasTranscript ? 'Continue Answer' : 'Start Answer'}</Button>}
      {hasTranscript && !listening && <Button variant="secondary" onClick={onRecordAgain} disabled={disabled} aria-label="Clear transcript and record answer again"><FiRefreshCw />Record Again</Button>}
    </div>
    <p className="mt-3 text-sm font-medium" role="status" aria-live="polite">{listening ? 'Listening — pause naturally and continue speaking. Recording stops only when you press Stop Answer.' : hasTranscript ? 'Recording stopped. Review and edit your transcript.' : 'Ready to record.'}</p>
  </div>
}
