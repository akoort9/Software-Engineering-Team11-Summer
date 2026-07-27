import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  addMovie,
  fetchMovies,
  fetchShowrooms,
  scheduleShowtime,
} from '../api/movies.js'
import { useAuth } from '../auth/AuthContext.jsx'
import '../App.css'
import '../styles/AuthPage.css'

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
  endTime: '',
}

export default function AdminPage() {
  const { user } = useAuth()
  const [section, setSection] = useState('movies')

  const [movies, setMovies] = useState([])
  const [showrooms, setShowrooms] = useState([])

  const [movieForm, setMovieForm] = useState(EMPTY_MOVIE)
  const [showtimeForm, setShowtimeForm] = useState(EMPTY_SHOWTIME)

  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    Promise.all([fetchMovies().then(setMovies), fetchShowrooms().then(setShowrooms)])
      .catch((err) => setError(err.message))
  }, [])

  const updateMovieField = (e) =>
    setMovieForm({ ...movieForm, [e.target.name]: e.target.value })
  const updateShowtimeField = (e) =>
    setShowtimeForm({ ...showtimeForm, [e.target.name]: e.target.value })

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

    const { movieId, showroomId, startTime, endTime } = showtimeForm
    if (!movieId || !showroomId) {
      setError('Please choose a movie and a showroom.')
      return
    }
    if (!startTime || !endTime) {
      setError('Start and end times are required.')
      return
    }
    if (endTime <= startTime) {
      setError('End time must be after the start time.')
      return
    }

    const movie = movies.find((m) => String(m.id) === movieId)

    setSubmitting(true)
    try {
      await scheduleShowtime(user.email, {
        movie: { id: movie.id, title: movie.title },
        showroomID: Number(showroomId),
        startTime,
        endTime,
      })
      setShowtimeForm(EMPTY_SHOWTIME)
      setStatus('Showtime scheduled.')
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (!user || !user.isAdmin) {
    return (
      <main className="auth-page">
        <div className="auth-card">
          <h1>Admin</h1>
          <p className="auth-message">
            This page requires an administrator account.
          </p>
          <Link to="/login" className="auth-button">Go to Login</Link>
        </div>
      </main>
    )
  }

  return (
    <main className="auth-page">
      <div className="auth-card profile-card">
        <h1>Admin</h1>
        <p className="profile-signed-in">
          Signed in as <strong>{user.email}</strong>
        </p>

        <div className="admin-menu">
          {MENU.map((item) => (
            <button
              key={item.id}
              type="button"
              className={`filter-toggle ${section === item.id ? 'active' : ''}`}
              onClick={() => switchSection(item.id)}
            >
              {item.label}
            </button>
          ))}
        </div>

        {status && <div className="profile-alert success">{status}</div>}
        {error && <div className="profile-alert error">{error}</div>}

        {section === 'movies' && (
          <section className="profile-section">
            <h2>Add Movie</h2>
            <p className="field-hint">All fields are required.</p>

            <form className="auth-form" onSubmit={handleAddMovie}>
              <div className="form-group">
                <label htmlFor="title">Title *</label>
                <input id="title" name="title" value={movieForm.title} onChange={updateMovieField} />
              </div>

              <div className="form-group">
                <label htmlFor="genre">Genre *</label>
                <input id="genre" name="genre" value={movieForm.genre} onChange={updateMovieField} />
              </div>

              <div className="form-group">
                <label htmlFor="desc">Description *</label>
                <textarea id="desc" name="desc" rows="3" value={movieForm.desc} onChange={updateMovieField} />
              </div>

              <div className="form-group">
                <label htmlFor="poster">Poster URL *</label>
                <input id="poster" name="poster" value={movieForm.poster} onChange={updateMovieField} placeholder="https://..." />
              </div>

              <div className="form-group">
                <label htmlFor="trailer">Trailer URL *</label>
                <input id="trailer" name="trailer" value={movieForm.trailer} onChange={updateMovieField} placeholder="https://youtube.com/watch?v=..." />
              </div>

              <div className="form-group">
                <label htmlFor="rating">Rating (0-10) *</label>
                <input id="rating" name="rating" type="number" min="0" max="10" value={movieForm.rating} onChange={updateMovieField} />
              </div>

              <div className="form-group">
                <label htmlFor="movie-status">Status *</label>
                <select id="movie-status" name="status" value={movieForm.status} onChange={updateMovieField}>
                  <option value="true">Currently Running</option>
                  <option value="false">Coming Soon</option>
                </select>
              </div>

              <button type="submit" className="auth-button" disabled={submitting}>
                {submitting ? 'Saving...' : 'Add Movie'}
              </button>
            </form>
          </section>
        )}

        {section === 'showtimes' && (
          <section className="profile-section">
            <h2>Schedule Showtime</h2>
            <p className="field-hint">
              All fields are required. Times that overlap an existing showtime
              in the same showroom will be rejected.
            </p>

            <form className="auth-form" onSubmit={handleScheduleShowtime}>
              <div className="form-group">
                <label htmlFor="movieId">Movie *</label>
                <select id="movieId" name="movieId" value={showtimeForm.movieId} onChange={updateShowtimeField}>
                  <option value="">Select a movie...</option>
                  {movies.map((movie) => (
                    <option key={movie.id} value={movie.id}>{movie.title}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="showroomId">Showroom *</label>
                <select id="showroomId" name="showroomId" value={showtimeForm.showroomId} onChange={updateShowtimeField}>
                  <option value="">Select a showroom...</option>
                  {showrooms.map((room) => (
                    <option key={room.id} value={room.id}>
                      {room.name} ({room.capacity} seats)
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="startTime">Start Time *</label>
                <input id="startTime" name="startTime" type="datetime-local" value={showtimeForm.startTime} onChange={updateShowtimeField} />
              </div>

              <div className="form-group">
                <label htmlFor="endTime">End Time *</label>
                <input id="endTime" name="endTime" type="datetime-local" value={showtimeForm.endTime} onChange={updateShowtimeField} />
              </div>

              <button type="submit" className="auth-button" disabled={submitting}>
                {submitting ? 'Scheduling...' : 'Schedule Showtime'}
              </button>
            </form>
          </section>
        )}

        {section === 'promotions' && (
          <section className="profile-section">
            <h2>Manage Promotions</h2>
            <p className="field-hint">Coming in a future sprint.</p>
          </section>
        )}

        {section === 'users' && (
          <section className="profile-section">
            <h2>Manage Users</h2>
            <p className="field-hint">Coming in a future sprint.</p>
          </section>
        )}

        <p className="profile-back">
          <Link to="/">Back to home</Link>
        </p>
      </div>
    </main>
  )
}
