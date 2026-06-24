import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchMovie } from '../api/movies.js'
import { statusLabel } from '../utils/statusLabel.js'
import { embedUrl } from '../utils/youtube.js'
import '../App.css'

function parseShowtimes(showtimes) {
  if (!showtimes) return []
  return showtimes
    .split(';')
    .flatMap((group) => group.split(','))
    .map((time) => time.trim())
    .filter(Boolean)
}

export default function MovieDetailPage() {
  const { id } = useParams()
  const [movie, setMovie] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchMovie(id)
      .then(setMovie)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <main className="detail"><p>Loading...</p></main>
  if (error) return <main className="detail"><p className="error">{error}</p></main>
  if (!movie) {
    return (
      <main className="detail">
        <p>Movie not found.</p>
        <p><Link to="/">Back to home</Link></p>
      </main>
    )
  }

  const showtimes = parseShowtimes(movie.showtimes)
  const trailer = embedUrl(movie.trailer)

  return (
    <main className="detail">
      <p className="detail-back"><Link to="/">Back to movies</Link></p>

      <div className="detail-top">
        {movie.poster && (
          <img className="detail-poster" src={movie.poster} alt={`${movie.title} poster`} />
        )}

        <div className="detail-info">
          <h1>{movie.title}</h1>
          <p className="detail-meta">
            <span className="badge">{statusLabel(movie.status)}</span>
            {movie.genre && <span className="badge">{movie.genre}</span>}
            <span className="badge">{movie.rating}/10</span>
          </p>
          <p className="detail-desc">{movie.desc}</p>

          <h2>Showtimes</h2>
          {showtimes.length > 0 ? (
            <ul className="showtimes">
              {showtimes.map((time) => (
                <li key={time} className="showtime">{time}</li>
              ))}
            </ul>
          ) : (
            <p>No showtimes listed.</p>
          )}
        </div>
      </div>

      <h2>Trailer</h2>
      {trailer ? (
        <div className="trailer">
          <iframe
            src={trailer}
            title={`${movie.title} trailer`}
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
          />
        </div>
      ) : (
        <p>No trailer available.</p>
      )}
    </main>
  )
}
