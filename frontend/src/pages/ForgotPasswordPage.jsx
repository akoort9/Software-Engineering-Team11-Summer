import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { requestPasswordReset, verifyResetCode } from '../api/users.js'
import '../styles/AuthPage.css'

export default function ForgotPasswordPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [code, setCode] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSendCode = async (e) => {
    e.preventDefault()

    if (!email.trim()) {
      setError('Please enter your email.')
      return
    }

    setSubmitting(true)
    setError('')

    try {
      await requestPasswordReset({ email: email.trim() })
      setCodeSent(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const handleVerifyCode = async (e) => {
    e.preventDefault()

    if (!code.trim()) {
      setError('Please enter the verification code.')
      return
    }

    setSubmitting(true)
    setError('')

    try {
      const trimmedEmail = email.trim()
      const trimmedCode = code.trim()
      await verifyResetCode({ email: trimmedEmail, code: trimmedCode })
      navigate(`/reset-password?email=${encodeURIComponent(trimmedEmail)}`, {
        state: { code: trimmedCode },
      })
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <div className="auth-card">
        <h1>Forgot Password</h1>
        <p className="auth-message">
          {codeSent
            ? 'Enter the verification code sent to your email.'
            : 'Enter your email to receive a password reset code.'}
        </p>

        {error && <p className="auth-error">{error}</p>}

        {!codeSent ? (
          <form className="auth-form" onSubmit={handleSendCode}>
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input
                id="email"
                type="email"
                placeholder="Enter your email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <button type="submit" className="auth-button" disabled={submitting}>
              {submitting ? 'Sending...' : 'Send Reset Code'}
            </button>

            <p className="auth-switch">
              Remembered your password? <Link to="/login">Log in</Link>
            </p>
          </form>
        ) : (
          <form className="auth-form" onSubmit={handleVerifyCode}>
            <div className="form-group">
              <label htmlFor="code">Verification Code</label>
              <input
                id="code"
                type="text"
                placeholder="Enter your 6-digit code"
                value={code}
                onChange={(e) => setCode(e.target.value)}
              />
            </div>

            <button type="submit" className="auth-button" disabled={submitting}>
              {submitting ? 'Verifying...' : 'Verify Code'}
            </button>

            <p className="auth-switch">
              Wrong email?{' '}
              <button
                type="button"
                className="link-plain"
                onClick={() => {
                  setCodeSent(false)
                  setCode('')
                  setError('')
                }}
              >
                Try again
              </button>
            </p>
          </form>
        )}
      </div>
    </main>
  )
}
