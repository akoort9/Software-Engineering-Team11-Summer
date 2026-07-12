import '../styles/AuthPage.css'

export default function EditProfilePage() {
  return (
    <main className="auth-page">
      <div className="auth-card">
        <h1>Edit Profile</h1>
        <form className="auth-form" onSubmit={(e) => e.preventDefault()}>
          <div className="form-group">
            <label htmlFor="first-name">First Name</label>
            <input id="first-name" type="text" placeholder="Edit your first name" />
          </div>

          <div className="form-group">
            <label htmlFor="last-name">Last Name</label>
            <input id="last-name" type="text" placeholder="Edit your last name" />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" placeholder="Change your email" />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input id="password" type="password" placeholder="Change your password" />
          </div>

          <button type="submit" className="auth-button">
            Save Changes
          </button>
        </form>
      </div>
    </main>
  )
}
