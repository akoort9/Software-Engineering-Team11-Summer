import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { addMovie, fetchMovies } from '../api/movies.js'
import '../App.css'

export default function AdminPage() {
  const [title, setTitle] = useState('')
  const [movies, setMovies] = useState([])
  const [error, setError] = useState('')

  const loadMovies = () =>
    fetchMovies()
      .then(setMovies)
      .catch((err) => setError(err.message))

  useEffect(() => {
    loadMovies()
  }, [])

  const handleKeyDown = async (e) => {
    if (e.key !== 'Enter') return

    const trimmed = title.trim()
    if (!trimmed) return

    try {
      await addMovie(trimmed)
      setTitle('')
      setError('')
      await loadMovies()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <main className="app">
      <h1>Add a Movie</h1>

      <input
        type="text"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="Movie title, then press Enter"
        className="movie-input"
        autoFocus
      />

      {error && <p className="error">{error}</p>}

      <ul className="movie-list">
        {movies.map((movie, i) => (
          <li key={i}>{movie.title}</li>
        ))}
      </ul>

      <p>
        <Link to="/">Back to home</Link>
      </p>
    </main>
  )
}
