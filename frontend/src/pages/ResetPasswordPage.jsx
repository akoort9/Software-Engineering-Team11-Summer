import { useState } from 'react'
import { Link, useLocation, useSearchParams } from 'react-router-dom'
import { resetPassword } from '../api/users.js'
import '../styles/AuthPage.css'

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const location = useLocation()

  const email = searchParams.get('email') || ''
  const code = (location.state && location.state.code) || ''

  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [done, setDone] = useState(false)

  if (!email || !code) {
    return (
      <main className="auth-page">
        <div className="auth-card">
          <h1>Reset Password</h1>
          <p className="auth-message">
            Your password reset session has expired or is invalid.
          </p>
          <Link to="/forgot-password" className="auth-button">
            Request a New Code
          </Link>
        </div>
      </main>
    )
  }

  if (done) {
    return (
      <main className="auth-page">
        <div className="auth-card">
          <h1>Password Reset</h1>
          <p className="auth-message">
            Your password has been updated. You can now log in.
          </p>
          <Link to="/login" className="auth-button">
            Go to Login
          </Link>
        </div>
      </main>
    )
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (password.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setSubmitting(true)
    setError('')

    try {
      await resetPassword({ email, code, newPassword: password })
      setDone(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <div className="auth-card">
        <h1>Reset Password</h1>
        <p className="auth-message">Choose a new password for {email}.</p>

        {error && <p className="auth-error">{error}</p>}

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="password">New Password</label>
            <input
              id="password"
              type="password"
              placeholder="Create a password (min 8 characters)"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <input
              id="confirmPassword"
              type="password"
              placeholder="Confirm your password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
          </div>

          <button type="submit" className="auth-button" disabled={submitting}>
            {submitting ? 'Resetting...' : 'Reset Password'}
          </button>
        </form>
      </div>
    </main>
  )
}
