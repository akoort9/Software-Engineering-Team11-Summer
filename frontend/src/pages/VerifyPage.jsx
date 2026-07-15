import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { verifyUser } from '../api/users.js'
import '../styles/AuthPage.css'

export default function VerifyPage() {
  const [searchParams] = useSearchParams()
  const [email, setEmail] = useState(searchParams.get('email') || '')
  const [code, setCode] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [verified, setVerified] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (!email.trim() || !code.trim()) {
      setError('Please enter your email and verification code.')
      return
    }

    setSubmitting(true)
    setError('')

    try {
      await verifyUser({ email: email.trim(), code: code.trim() })
      setVerified(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (verified) {
    return (
      <main className="auth-page">
        <div className="auth-card">
          <h1>Account Verified</h1>
          <p className="auth-message">
            Your account is now active. You can log in.
          </p>
          <Link to="/login" className="auth-button">
            Go to Login
          </Link>
        </div>
      </main>
    )
  }

  return (
    <main className="auth-page">
      <div className="auth-card">
        <h1>Verify Your Account</h1>
        <p className="auth-message">
          Enter the verification code sent to your email.
        </p>

        {error && <p className="auth-error">{error}</p>}

        <form className="auth-form" onSubmit={handleSubmit}>
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
            {submitting ? 'Verifying...' : 'Verify Account'}
          </button>

          <p className="auth-switch">
            Already verified? <Link to="/login">Log in</Link>
          </p>
        </form>
      </div>
    </main>
  )
}
