import { Fragment, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchMovies } from '../api/movies.js'
import { fetchAllShowtimes } from '../api/booking.js'
import { statusLabel } from '../utils/statusLabel.js'
import { formatShowtimeLabel } from '../utils/showtimes.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { useFavorites } from '../hooks/useFavorites.js'
import FavoriteStar from '../components/FavoriteStar.jsx'


export default function HomePage() {
  const { user, logout } = useAuth()
  const { isFavorited, toggleFavorite, canFavorite } = useFavorites()
  const [movies, setMovies] = useState([])
  const [showtimes, setShowtimes] = useState([])
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [genre, setGenre] = useState('')
  const [showSuggested, setShowSuggested] = useState(false)

  useEffect(() => {
    fetchMovies()
      .then(setMovies)
      .catch((err) => setError(err.message))
    fetchAllShowtimes()
      .then(setShowtimes)
      .catch(() => {})
  }, [])

  const genres = [...new Set(movies.map((movie) => movie.genre).filter(Boolean))]

 const favoritedGenres = new Set(
  movies
    .filter((movie) => isFavorited(movie.id))
    .map((movie) => movie.genre)
    .filter(Boolean)
)

const visibleMovies = movies.filter((movie) => {
  const matchesTitle = movie.title.toLowerCase().includes(search.toLowerCase())
  const matchesGenre = !genre || movie.genre === genre
  const matchesSuggested =
    !showSuggested || (favoritedGenres.has(movie.genre) && !isFavorited(movie.id))
  return matchesTitle && matchesGenre && matchesSuggested
})

  const sortedMovies = [...visibleMovies].sort(
  (a, b) => Number(Boolean(b.status)) - Number(Boolean(a.status))
)

return (
  <main className="home">
    <header className="top-bar">
      <h1>Movies</h1>
      <nav className="auth-nav">
        {user ? (
          <>
            <span>
              Welcome, {user.name || user.email}
              {user.isAdmin && ' (admin)'}
            </span>
            {user.isAdmin && <Link to="/admin">Admin</Link>}
            <Link to="/edit-profile">Edit Profile</Link>
            <button type="button" onClick={logout}>
              Log Out
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Log In</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </nav>
    </header>

      <input
        type="text"
        placeholder="Search by title"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />

      <select value={genre} onChange={(e) => setGenre(e.target.value)}>
        <option value="">All genres</option>
        {genres.map((g) => (
          <option key={g} value={g}>
            {g}
          </option>
        ))}
      </select>

      <select disabled>
        <option>Filter by date (coming soon)</option>
      </select>

      <button
        type="button"
        className={`filter-toggle${showSuggested ? ' active' : ''}`}
        onClick={() => setShowSuggested((prev) => !prev)}
      >
        Suggested
      </button>

      {error && <p>{error}</p>}

      {!error && visibleMovies.length === 0 && <p>No movies found.</p>}

      <div className="movie-grid">
  {sortedMovies.map((movie, index) => {
    const isFirstComingSoon =
      !movie.status && (index === 0 || sortedMovies[index - 1].status)

    return (
      <Fragment key={movie.id}>
        {isFirstComingSoon && <div className="grid-divider" />}
       <article className="movie-card">
  {movie.poster && (
    <img
      className="movie-poster"
      src={movie.poster}
      alt={`${movie.title} poster`}
    />
  )}
    <div className="movie-card-header">
      <h2>
        <Link to={`/movies/${movie.id}`}>{movie.title}</Link>
      </h2>
        {canFavorite && (
        <FavoriteStar
          favorited={isFavorited(movie.id)}
          onClick={() => toggleFavorite(movie.id)}
        />
     )}
    </div>
          <p className="movie-genre">{movie.genre}</p>
          <p className="movie-status">{statusLabel(movie.status)}</p>
          {(() => {
            const movieShowtimes = showtimes.filter((st) => st.movieId === movie.id)
            return movieShowtimes.length > 0 && movie.status ? (
              <ul className="showtimes">
                {movieShowtimes.map((st) => (
                  <li key={st.id}>
                    <Link
                      to={`/booking?movieId=${movie.id}&showtimeId=${st.id}`}
                      className="showtime"
                    >
                      {formatShowtimeLabel(st)}
                    </Link>
                  </li>
                ))}
              </ul>
            ) : (
              <p>No showtimes listed.</p>
            )
          })()}
        </article>
      </Fragment>
    )
  })}
</div>

      {user ? (
        <p>
          Welcome, {user.name || user.email}
          {user.isAdmin && ' (admin)'} |{' '}
          <Link to="/booking">Book Seats</Link> |{' '}
          {user.isAdmin && (
            <>
              <Link to="/admin">Admin</Link> |{' '}
            </>
          )}
          <Link to="/edit-profile">Edit Profile</Link> |{' '}
          <button type="button" onClick={logout}>
            Log Out
          </button>
        </p>
      ) : (
        <div className="book-seats-cta">
          <Link to="/booking" className="book-seats-btn">
            Book Seats
          </Link>
    </div>
      )}
    </main>
  )
}
