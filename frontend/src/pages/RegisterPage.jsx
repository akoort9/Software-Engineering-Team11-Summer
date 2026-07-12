import '../styles/AuthPage.css'

export default function RegisterPage() {
  return (
    <main className="auth-page">
      <div className="auth-card">
        <h1>Create Account</h1>
        <form className="auth-form" onSubmit={(e) => e.preventDefault()}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" placeholder="Enter your email" />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input id="password" type="password" placeholder="Create a password" />
          </div>

          <div className="form-group">
            <label htmlFor="confirm-password">Confirm Password</label>
            <input
              id="confirm-password"
              type="password"
              placeholder="Confirm your password"
            />
          </div>

          <button type="submit" className="auth-button">
            Register
          </button>
        </form>
      </div>
    </main>
  )
}
