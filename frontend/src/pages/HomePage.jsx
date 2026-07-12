import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchMovies } from '../api/movies.js'
import { statusLabel } from '../utils/statusLabel.js'
import { getShowtimes } from '../utils/showtimes.js'

export default function HomePage() {
  const [movies, setMovies] = useState([])
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [genre, setGenre] = useState('')

  useEffect(() => {
    fetchMovies()
      .then(setMovies)
      .catch((err) => setError(err.message))
  }, [])

  const genres = [...new Set(movies.map((movie) => movie.genre).filter(Boolean))]

  const visibleMovies = movies.filter((movie) => {
    const matchesTitle = movie.title
      .toLowerCase()
      .includes(search.toLowerCase())
    const matchesGenre = !genre || movie.genre === genre
    return matchesTitle && matchesGenre
  })

  return (
    <main>
      <h1>Movies</h1>

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

      {error && <p>{error}</p>}

      {!error && visibleMovies.length === 0 && <p>No movies found.</p>}

      <div className="movie-grid">
        {visibleMovies.map((movie) => (
          <article key={movie.id} className="movie-card">
            <h2>
              <Link to={`/movies/${movie.id}`}>{movie.title}</Link>
            </h2>
              <p className="movie-genre">{movie.genre}</p>
              <p className="movie-status">{statusLabel(movie.status)}</p>
	      {getShowtimes(movie).length > 0 ? (  
		<ul className="showtimes">
		    {getShowtimes(movie).map((time) => (
			<li key={time}>
			    <Link
				to={`/booking?movieId=${encodeURIComponent(movie.title)}&showtime=${encodeURIComponent(time)}`}
				className="showtime"
			    >
				{time}
			    </Link>
			</li>
		  ))}
		</ul>
	      ) : (
		  <p>No showtimes listed.</p>
	      )}
          </article>
        ))}
      </div>

      <p>
        <Link to="/booking">Book Seats</Link> | <Link to="/admin">Admin</Link> |{' '}
        <Link to="/login">Log In</Link> | <Link to="/register">Register</Link>
      </p>
    </main>
  )
}
