import { useState } from 'react'
import { Link } from 'react-router-dom'
import { registerUser } from '../api/users.js'
import '../styles/AuthPage.css'

const EMPTY_FORM = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  subscribeToPromotions: false,
}

export default function RegisterPage() {
  const [form, setForm] = useState(EMPTY_FORM)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [success, setSuccess] = useState(false)
  const [registeredEmail, setRegisteredEmail] = useState('')
  const [verificationCode, setVerificationCode] = useState('')

  const updateField = (e) => {
    const { name, value, type, checked } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }))
  }

  const validate = () => {
    if (!form.firstName.trim()) return 'First name is required.'
    if (!form.lastName.trim()) return 'Last name is required.'
    if (!form.email.trim()) return 'Email is required.'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      return 'Please enter a valid email address.'
    }
    if (form.password.length < 8) {
      return 'Password must be at least 8 characters.'
    }
    if (form.password !== form.confirmPassword) {
      return 'Passwords do not match.'
    }
    return ''
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    const validationError = validate()
    if (validationError) {
      setError(validationError)
      return
    }

    setSubmitting(true)
    setError('')

    try {
      const data = await registerUser({
        name: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        password: form.password,
        mailingAddress: '',
        state: 'INACTIVE',
        subscribedToPromotions: form.subscribeToPromotions,
        isAdmin: false,
      })
      setRegisteredEmail(form.email.trim())
      setVerificationCode(data && data.verificationCode ? data.verificationCode : '')
      setSuccess(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (success) {
    return (
      <main className="auth-page">
        <div className="auth-card">
          <h1>Check Your Email</h1>
          <p className="auth-message">
            Your account has been created but is not yet verified. Enter the
            verification code sent to your email to activate your account.
          </p>
          {verificationCode && (
            <p className="auth-message">
              <strong>Demo:</strong> email delivery is simulated. Your
              verification code is <strong>{verificationCode}</strong>.
            </p>
          )}
          <Link
            to={`/verify?email=${encodeURIComponent(registeredEmail)}`}
            className="auth-button"
          >
            Verify Account
          </Link>
        </div>
      </main>
    )
  }

  return (
    <main className="auth-page">
      <div className="auth-card">
        <h1>Create Account</h1>
        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="firstName">First Name *</label>
            <input
              id="firstName"
              name="firstName"
              type="text"
              value={form.firstName}
              onChange={updateField}
              placeholder="Enter your first name"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="lastName">Last Name *</label>
            <input
              id="lastName"
              name="lastName"
              type="text"
              value={form.lastName}
              onChange={updateField}
              placeholder="Enter your last name"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email *</label>
            <input
              id="email"
              name="email"
              type="email"
              value={form.email}
              onChange={updateField}
              placeholder="Enter your email"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password *</label>
            <input
              id="password"
              name="password"
              type="password"
              value={form.password}
              onChange={updateField}
              placeholder="Create a password (min 8 characters)"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm Password *</label>
            <input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              value={form.confirmPassword}
              onChange={updateField}
              placeholder="Confirm your password"
              required
            />
          </div>

          <div className="form-checkbox">
            <input
              id="subscribeToPromotions"
              name="subscribeToPromotions"
              type="checkbox"
              checked={form.subscribeToPromotions}
              onChange={updateField}
            />
            <label htmlFor="subscribeToPromotions">
              Subscribe to promotional emails
            </label>
          </div>

          {error && <p className="auth-error">{error}</p>}

          <button type="submit" className="auth-button" disabled={submitting}>
            {submitting ? 'Creating account...' : 'Register'}
          </button>

          <p className="auth-switch">
            Already have an account? <Link to="/login">Log in</Link>
          </p>
        </form>
      </div>
    </main>
  )
}
