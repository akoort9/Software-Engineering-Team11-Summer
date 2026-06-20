import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [title, setTitle] = useState('')
  const [movies, setMovies] = useState([])
  const [error, setError] = useState('')

  const loadMovies = async () => {
    try {
      const res = await fetch('/api/movies')
      if (!res.ok) throw new Error('Failed to load movies')
      setMovies(await res.json())
    } catch (err) {
      setError(err.message)
    }
  }

  useEffect(() => {
    loadMovies()
  }, [])

  const handleKeyDown = async (e) => {
    if (e.key !== 'Enter') return

    const trimmed = title.trim()
    if (!trimmed) return

    try {
      const res = await fetch('/api/movies', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: trimmed }),
      })

      if (!res.ok) throw new Error('Failed to save movie')

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
    </main>
  )
}

export default App
