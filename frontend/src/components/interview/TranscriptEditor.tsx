interface Props {
  value: string
  interimTranscript: string
  listening: boolean
  readOnly?: boolean
  onChange: (value: string) => void
}

export function TranscriptEditor({ value, interimTranscript, listening, readOnly, onChange }: Props) {
  return <div>
    <label htmlFor="voice-transcript" className="block text-sm font-medium">{readOnly ? 'Saved transcript' : 'Review transcript'}</label>
    {listening && <div className="mt-2 min-h-12 rounded-xl bg-indigo-50 p-3 text-sm dark:bg-indigo-950/30" aria-live="polite">
      <span>{value}</span>{interimTranscript && <span className="italic text-slate-500"> {interimTranscript}</span>}
    </div>}
    <textarea id="voice-transcript" value={value} onChange={event => onChange(event.target.value)} readOnly={readOnly} rows={8} placeholder="Your speech transcript will appear here. You can edit it before saving." className="mt-2 w-full resize-y rounded-xl border border-slate-300 bg-white p-3.5 text-sm leading-6 outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-500/15 read-only:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:read-only:bg-slate-800/60" />
  </div>
}
