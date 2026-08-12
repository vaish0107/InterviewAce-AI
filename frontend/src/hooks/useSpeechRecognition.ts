import { useCallback, useEffect, useRef, useState } from 'react'

const fatalRecognitionErrors: Record<string, string> = {
  'not-allowed': 'Microphone access was denied or blocked. Allow microphone access in your browser settings and try again.',
  'service-not-allowed': 'Speech recognition is blocked or unavailable in this browser.',
  'audio-capture': 'No working microphone was found.',
  network: 'Speech recognition lost its connection. Check your connection and start recording again.',
}

const RESTART_DELAY_MS = 250

export function useSpeechRecognition(language = 'en-US') {
  const recognitionRef = useRef<SpeechRecognition | null>(null)
  const permissionGrantedRef = useRef(false)
  const shouldKeepListeningRef = useRef(false)
  const recognitionActiveRef = useRef(false)
  const unmountingRef = useRef(false)
  const fatalErrorRef = useRef(false)
  const restartTimerRef = useRef<number | null>(null)
  const [transcript, setTranscript] = useState('')
  const [interimTranscript, setInterimTranscript] = useState('')
  const [listening, setListening] = useState(false)
  const [error, setError] = useState('')
  const supported = typeof window !== 'undefined' && Boolean(window.SpeechRecognition || window.webkitSpeechRecognition)

  const clearRestartTimer = useCallback(() => {
    if (restartTimerRef.current !== null) {
      window.clearTimeout(restartTimerRef.current)
      restartTimerRef.current = null
    }
  }, [])

  const createAndStartRecognition = useCallback(() => {
    if (!shouldKeepListeningRef.current || unmountingRef.current || fatalErrorRef.current || recognitionActiveRef.current) return
    const Recognition = window.SpeechRecognition || window.webkitSpeechRecognition
    if (!Recognition) return

    const recognition = new Recognition()
    recognition.continuous = true
    recognition.interimResults = true
    recognition.lang = language
    recognition.onstart = () => {
      recognitionActiveRef.current = true
      setListening(true)
    }
    recognition.onresult = event => {
      let finalText = ''
      let interimText = ''
      for (let i = event.resultIndex; i < event.results.length; i += 1) {
        const text = event.results[i][0].transcript
        if (event.results[i].isFinal) finalText += text
        else interimText += text
      }
      if (finalText.trim()) {
        setTranscript(current => `${current}${current && !current.endsWith(' ') ? ' ' : ''}${finalText.trim()}`)
      }
      setInterimTranscript(interimText)
    }
    recognition.onerror = event => {
      recognitionActiveRef.current = false
      if (event.error === 'no-speech') {
        setInterimTranscript('')
        return
      }
      if (event.error === 'aborted' && !shouldKeepListeningRef.current) return

      const fatalMessage = fatalRecognitionErrors[event.error]
      if (fatalMessage) {
        fatalErrorRef.current = true
        shouldKeepListeningRef.current = false
        clearRestartTimer()
        setError(fatalMessage)
        setListening(false)
        return
      }
      setError(`Speech recognition stopped: ${event.message || event.error}.`)
      fatalErrorRef.current = true
      shouldKeepListeningRef.current = false
      setListening(false)
    }
    recognition.onend = () => {
      if (recognitionRef.current !== recognition) return
      recognitionActiveRef.current = false
      setInterimTranscript('')
      if (shouldKeepListeningRef.current && !fatalErrorRef.current && !unmountingRef.current) {
        clearRestartTimer()
        restartTimerRef.current = window.setTimeout(() => {
          restartTimerRef.current = null
          createAndStartRecognition()
        }, RESTART_DELAY_MS)
      } else {
        setListening(false)
      }
    }
    recognitionRef.current = recognition
    try {
      recognition.start()
      setListening(true)
    } catch (startError) {
      console.error('Speech recognition failed to start', startError)
      recognitionActiveRef.current = false
      if (startError instanceof DOMException && startError.name === 'InvalidStateError' && shouldKeepListeningRef.current) {
        clearRestartTimer()
        restartTimerRef.current = window.setTimeout(createAndStartRecognition, RESTART_DELAY_MS)
      } else {
        shouldKeepListeningRef.current = false
        setListening(false)
        setError('Speech recognition could not start. Please try again.')
      }
    }
  }, [clearRestartTimer, language])

  const stopListening = useCallback(() => {
    shouldKeepListeningRef.current = false
    fatalErrorRef.current = false
    clearRestartTimer()
    setInterimTranscript('')
    setListening(false)
    if (recognitionActiveRef.current) recognitionRef.current?.stop()
  }, [clearRestartTimer])

  const clearTranscript = useCallback(() => {
    setTranscript('')
    setInterimTranscript('')
    setError('')
  }, [])

  const startListening = useCallback(async () => {
    if (!supported) {
      setError('Voice recognition is not supported in this browser. You can continue using Text Interview mode.')
      return
    }
    if (shouldKeepListeningRef.current && recognitionActiveRef.current) return
    if (recognitionActiveRef.current) {
      recognitionRef.current?.abort()
      recognitionActiveRef.current = false
    }
    clearRestartTimer()
    unmountingRef.current = false
    fatalErrorRef.current = false
    setError('')
    try {
      if (!permissionGrantedRef.current) {
        if (!navigator.mediaDevices?.getUserMedia) throw new Error('unavailable')
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
        stream.getTracks().forEach(track => track.stop())
        permissionGrantedRef.current = true
      }
      shouldKeepListeningRef.current = true
      unmountingRef.current = false
      createAndStartRecognition()
    } catch (permissionError) {
      shouldKeepListeningRef.current = false
      const name = permissionError instanceof DOMException ? permissionError.name : ''
      setError(name === 'NotAllowedError' || name === 'SecurityError'
        ? 'Microphone access was denied or blocked. Allow microphone access in your browser settings and try again.'
        : name === 'NotFoundError' || name === 'DevicesNotFoundError'
          ? 'No microphone is available. Connect a microphone and try again.'
          : 'Your browser could not access a microphone.')
      setListening(false)
    }
  }, [clearRestartTimer, createAndStartRecognition, supported])

  useEffect(() => {
    unmountingRef.current = false
    return () => {
      unmountingRef.current = true
      shouldKeepListeningRef.current = false
      recognitionActiveRef.current = false
      clearRestartTimer()
      recognitionRef.current?.abort()
    }
  }, [clearRestartTimer])

  return { supported, listening, transcript, interimTranscript, error, startListening, stopListening, clearTranscript, setTranscript }
}
