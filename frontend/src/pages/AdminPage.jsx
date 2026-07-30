import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  addMovie,
  fetchMovies,
  fetchShowrooms,
  scheduleShowtimes,
} from '../api/movies.js'
import {
  fetchUsers,
  setUserState,
  fetchPromotions,
  createPromotion,
} from '../api/admin.js'
import { useAuth } from '../auth/AuthContext.jsx'
import '../styles/Admin.css'

const MENU = [
  { id: 'movies', label: 'Manage Movies' },
  { id: 'showtimes', label: 'Manage Showtimes' },
  { id: 'promotions', label: 'Manage Promotions' },
  { id: 'users', label: 'Manage Users' },
]

const EMPTY_MOVIE = {
  title: '',
  genre: '',
  desc: '',
  poster: '',
  trailer: '',
  rating: '',
  status: 'true',
}

const EMPTY_SHOWTIME = {
  movieId: '',
  showroomId: '',
  startTime: '',
  durationMinutes: '120',
  repeatDays: '1',
}

const EMPTY_PROMOTION = {
  promoCode: '',
  percentOff: '',
  expirationDate: '',
}

export default function AdminPage() {
  const { user } = useAuth()
  const [section, setSection] = useState('movies')

  const [movies, setMovies] = useState([])
  const [showrooms, setShowrooms] = useState([])
  const [users, setUsers] = useState([])
  const [promotions, setPromotions] = useState([])

  const [movieForm, setMovieForm] = useState(EMPTY_MOVIE)
  const [showtimeForm, setShowtimeForm] = useState(EMPTY_SHOWTIME)
  const [promotionForm, setPromotionForm] = useState(EMPTY_PROMOTION)

  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!user || !user.isAdmin) {
      return
    }
    Promise.all([
      fetchMovies().then(setMovies),
      fetchShowrooms().then(setShowrooms),
      fetchUsers(user.email).then(setUsers),
      fetchPromotions(user.email).then(setPromotions),
    ]).catch((err) => setError(err.message))
  }, [user])

  const updateMovieField = (e) =>
    setMovieForm({ ...movieForm, [e.target.name]: e.target.value })
  const updateShowtimeField = (e) =>
    setShowtimeForm({ ...showtimeForm, [e.target.name]: e.target.value })
  const updatePromotionField = (e) =>
    setPromotionForm({ ...promotionForm, [e.target.name]: e.target.value })

  const switchSection = (id) => {
    setSection(id)
    setStatus('')
    setError('')
  }

  const handleAddMovie = async (e) => {
    e.preventDefault()
    setError('')
    setStatus('')

    const rating = Number(movieForm.rating)
    if (!movieForm.title.trim()) {
      setError('Title is required.')
      return
    }
    if (!movieForm.genre.trim() || !movieForm.desc.trim()) {
      setError('Genre and description are required.')
      return
    }
    if (!movieForm.poster.trim() || !movieForm.trailer.trim()) {
      setError('Poster and trailer URLs are required.')
      return
    }
    if (movieForm.rating === '' || !Number.isInteger(rating) || rating < 0 || rating > 10) {
      setError('Rating must be a whole number from 0 to 10.')
      return
    }

    setSubmitting(true)
    try {
      await addMovie(user.email, {
        title: movieForm.title.trim(),
        genre: movieForm.genre.trim(),
        desc: movieForm.desc.trim(),
        poster: movieForm.poster.trim(),
        trailer: movieForm.trailer.trim(),
        rating,
        status: movieForm.status === 'true',
      })
      setMovieForm(EMPTY_MOVIE)
      setMovies(await fetchMovies())
      setStatus('Movie added.')
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const handleScheduleShowtime = async (e) => {
    e.preventDefault()
    setError('')
    setStatus('')

    const { movieId, showroomId, startTime, durationMinutes, repeatDays } = showtimeForm
    const duration = Number(durationMinutes)
    const repeat = Number(repeatDays)
    if (!movieId || !showroomId) {
      setError('Please choose a movie and a showroom.')
      return
    }
    if (!startTime) {
      setError('A start time is required.')
      return
    }
    if (!Number.isInteger(duration) || duration <= 0) {
      setError('Duration must be a whole number of minutes greater than zero.')
      return
    }
    if (!Number.isInteger(repeat) || repeat < 1) {
      setError('Repeat must be a whole number of at least 1 day.')
      return
    }

    const movie = movies.find((m) => String(m.id) === movieId)

    setSubmitting(true)
    try {
      const result = await scheduleShowtimes(user.email, {
        movie: { id: movie.id, title: movie.title },
        showroomID: Number(showroomId),
        startTime,
        durationMinutes: duration,
        repeatDays: repeat,
      })
      setShowtimeForm(EMPTY_SHOWTIME)
      const skippedNote = result.skipped > 0 ? ` Skipped ${result.skipped} (conflicts).` : ''
      setStatus(`Scheduled ${result.created} showtime(s).${skippedNote}`)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const handleCreatePromotion = async (e) => {
    e.preventDefault()
    setError('')
    setStatus('')

    const percentOff = Number(promotionForm.percentOff)
    if (!promotionForm.promoCode.trim()) {
      setError('A promo code is required.')
      return
    }
    if (promotionForm.percentOff === '' || percentOff <= 0 || percentOff >= 100) {
      setError('Discount must be a number between 0 and 100.')
      return
    }
    if (!promotionForm.expirationDate) {
      setError('An expiration date is required.')
      return
    }

    setSubmitting(true)
    try {
      const result = await createPromotion(user.email, {
        promoCode: promotionForm.promoCode.trim(),
        percentOff,
        expirationDate: promotionForm.expirationDate,
      })
      setPromotionForm(EMPTY_PROMOTION)
      setPromotions(await fetchPromotions(user.email))
      setStatus(`Promotion created. Emailed ${result.emailsSent} subscriber(s).`)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const handleSetState = async (email, state) => {
    setError('')
    setStatus('')
    setSubmitting(true)
    try {
      await setUserState(user.email, email, state)
      setUsers(await fetchUsers(user.email))
      setStatus('User updated.')
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (!user || !user.isAdmin) {
    return (
      <main className="admin-page">
        <div className="admin-shell">
          <div className="admin-panel">
            <h2>Admin</h2>
            <p className="panel-hint">This page requires an administrator account.</p>
            <Link to="/login" className="admin-submit">Go to Login</Link>
          </div>
        </div>
      </main>
    )
  }

  return (
    <main className="admin-page">
      <div className="admin-shell">
        <div className="admin-header">
          <h1>Admin Console</h1>
          <p className="admin-signed-in">
            Signed in as <strong>{user.email}</strong>
          </p>
        </div>

        <div className="admin-layout">
          <nav className="admin-nav">
            {MENU.map((item) => (
              <button
                key={item.id}
                type="button"
                className={section === item.id ? 'active' : ''}
                onClick={() => switchSection(item.id)}
              >
                {item.label}
              </button>
            ))}
          </nav>

          <section className="admin-panel">
            {status && <div className="admin-alert success">{status}</div>}
            {error && <div className="admin-alert error">{error}</div>}

            {section === 'movies' && (
              <>
                <h2>Add Movie</h2>
                <p className="panel-hint">All fields are required.</p>

                <form className="admin-form" onSubmit={handleAddMovie}>
                  <div className="field">
                    <label htmlFor="title">Title</label>
                    <input id="title" name="title" value={movieForm.title} onChange={updateMovieField} />
                  </div>

                  <div className="field">
                    <label htmlFor="genre">Genre</label>
                    <input id="genre" name="genre" value={movieForm.genre} onChange={updateMovieField} />
                  </div>

                  <div className="field full">
                    <label htmlFor="desc">Description</label>
                    <textarea id="desc" name="desc" rows="3" value={movieForm.desc} onChange={updateMovieField} />
                  </div>

                  <div className="field">
                    <label htmlFor="poster">Poster URL</label>
                    <input id="poster" name="poster" value={movieForm.poster} onChange={updateMovieField} placeholder="https://..." />
                  </div>

                  <div className="field">
                    <label htmlFor="trailer">Trailer URL</label>
                    <input id="trailer" name="trailer" value={movieForm.trailer} onChange={updateMovieField} placeholder="https://youtube.com/watch?v=..." />
                  </div>

                  <div className="field">
                    <label htmlFor="rating">Rating (0-10)</label>
                    <input id="rating" name="rating" type="number" min="0" max="10" value={movieForm.rating} onChange={updateMovieField} />
                  </div>

                  <div className="field">
                    <label htmlFor="movie-status">Status</label>
                    <select id="movie-status" name="status" value={movieForm.status} onChange={updateMovieField}>
                      <option value="true">Currently Running</option>
                      <option value="false">Coming Soon</option>
                    </select>
                  </div>

                  <button type="submit" className="admin-submit" disabled={submitting}>
                    {submitting ? 'Saving...' : 'Add Movie'}
                  </button>
                </form>
              </>
            )}

            {section === 'showtimes' && (
              <>
                <h2>Schedule Showtime</h2>
                <p className="panel-hint">
                  The end time is set from the movie's duration. Repeat schedules the same
                  slot on consecutive days; slots that overlap an existing showtime in the
                  same showroom are skipped.
                </p>

                <form className="admin-form" onSubmit={handleScheduleShowtime}>
                  <div className="field">
                    <label htmlFor="movieId">Movie</label>
                    <select id="movieId" name="movieId" value={showtimeForm.movieId} onChange={updateShowtimeField}>
                      <option value="">Select a movie...</option>
                      {movies.map((movie) => (
                        <option key={movie.id} value={movie.id}>{movie.title}</option>
                      ))}
                    </select>
                  </div>

                  <div className="field">
                    <label htmlFor="showroomId">Showroom</label>
                    <select id="showroomId" name="showroomId" value={showtimeForm.showroomId} onChange={updateShowtimeField}>
                      <option value="">Select a showroom...</option>
                      {showrooms.map((room) => (
                        <option key={room.id} value={room.id}>
                          {room.name} ({room.capacity} seats)
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="field">
                    <label htmlFor="startTime">Start Time</label>
                    <input id="startTime" name="startTime" type="datetime-local" value={showtimeForm.startTime} onChange={updateShowtimeField} />
                  </div>

                  <div className="field">
                    <label htmlFor="durationMinutes">Duration (minutes)</label>
                    <input id="durationMinutes" name="durationMinutes" type="number" min="1" value={showtimeForm.durationMinutes} onChange={updateShowtimeField} />
                  </div>

                  <div className="field">
                    <label htmlFor="repeatDays">Repeat (days)</label>
                    <input id="repeatDays" name="repeatDays" type="number" min="1" value={showtimeForm.repeatDays} onChange={updateShowtimeField} />
                  </div>

                  <button type="submit" className="admin-submit" disabled={submitting}>
                    {submitting ? 'Scheduling...' : 'Schedule Showtimes'}
                  </button>
                </form>
              </>
            )}

            {section === 'promotions' && (
              <>
                <h2>Manage Promotions</h2>
                <p className="panel-hint">
                  Creating a promotion emails it to every customer subscribed to offers.
                </p>

                <form className="admin-form" onSubmit={handleCreatePromotion}>
                  <div className="field">
                    <label htmlFor="promoCode">Promo Code</label>
                    <input id="promoCode" name="promoCode" value={promotionForm.promoCode} onChange={updatePromotionField} placeholder="SUMMER25" />
                  </div>

                  <div className="field">
                    <label htmlFor="percentOff">Discount (% off)</label>
                    <input id="percentOff" name="percentOff" type="number" min="1" max="99" value={promotionForm.percentOff} onChange={updatePromotionField} />
                  </div>

                  <div className="field">
                    <label htmlFor="expirationDate">Expiration Date</label>
                    <input id="expirationDate" name="expirationDate" type="date" value={promotionForm.expirationDate} onChange={updatePromotionField} />
                  </div>

                  <button type="submit" className="admin-submit" disabled={submitting}>
                    {submitting ? 'Sending...' : 'Create & Send'}
                  </button>
                </form>

                <hr className="admin-divider" />
                <h2>Existing Promotions</h2>
                {promotions.length === 0 ? (
                  <p className="panel-hint">No promotions yet.</p>
                ) : (
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>Code</th>
                        <th>Discount</th>
                        <th>Expires</th>
                      </tr>
                    </thead>
                    <tbody>
                      {promotions.map((promo) => (
                        <tr key={promo.id}>
                          <td>{promo.promoCode}</td>
                          <td>{promo.percentOff}% off</td>
                          <td>{promo.expirationDate}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </>
            )}

            {section === 'users' && (
              <>
                <h2>Manage Users</h2>
                <p className="panel-hint">Suspend or reactivate customer accounts.</p>

                {users.length === 0 ? (
                  <p className="panel-hint">No users found.</p>
                ) : (
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody>
                      {users.map((u) => (
                        <tr key={u.id}>
                          <td>{[u.name, u.lastName].filter(Boolean).join(' ') || '-'}</td>
                          <td>{u.email}</td>
                          <td>{u.role}</td>
                          <td>
                            <span className={`state-badge ${u.state.toLowerCase()}`}>{u.state}</span>
                          </td>
                          <td>
                            {u.role === 'customer' && (
                              <div className="row-actions">
                                {u.state === 'SUSPENDED' ? (
                                  <button
                                    type="button"
                                    className="admin-btn"
                                    disabled={submitting}
                                    onClick={() => handleSetState(u.email, 'ACTIVE')}
                                  >
                                    Reactivate
                                  </button>
                                ) : (
                                  <button
                                    type="button"
                                    className="admin-btn danger"
                                    disabled={submitting}
                                    onClick={() => handleSetState(u.email, 'SUSPENDED')}
                                  >
                                    Suspend
                                  </button>
                                )}
                              </div>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </>
            )}
          </section>
        </div>

        <p className="admin-back">
          <Link to="/">Back to home</Link>
        </p>
      </div>
    </main>
  )
}
