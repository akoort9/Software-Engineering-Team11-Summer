import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchMovies } from '../api/movies.js'
import { statusLabel } from '../utils/statusLabel.js'

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

      <ul>
        {visibleMovies.map((movie) => (
          <li key={movie.id}>
            <Link to={`/movies/${movie.id}`}>{movie.title}</Link> — {statusLabel(movie.status)}
          </li>
        ))}
      </ul>

      <p>
        <Link to="/admin">Admin</Link>
      </p>
    </main>
  )
}
