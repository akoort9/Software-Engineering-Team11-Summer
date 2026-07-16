import { Link } from 'react-router-dom'
import '../styles/AuthPage.css'

export default function LoginPage() {
  return (
    <main className="auth-page">
      <div className="auth-card">
        <h1>Login</h1>
        <form className="auth-form" onSubmit={(e) => e.preventDefault()}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" placeholder="Enter your email" />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input id="password" type="password" placeholder="Enter your password" />
          </div>

          <div className="auth-actions">
            <button type="submit" className="auth-button">
              Log In
            </button>
            <Link to="/register" className="forgot-password">
              Forgot your password?
            </Link>
          </div>
        </form>
      </div>
    </main>
  )
}
