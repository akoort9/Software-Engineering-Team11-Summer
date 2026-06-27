import { useEffect, useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { fetchMovies } from '../api/movies.js'
import { getShowtimes } from '../utils/showtimes.js'
import '../styles/BookingPage.css'

export default function BookingPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  
  const [movies, setMovies] = useState([])
  const [selectedMovieId, setSelectedMovieId] = useState(
    searchParams.get('movieId') || ''
  )
  const [showtime, setShowtime] = useState(searchParams.get('showtime') || '')
  const [ticketCounts, setTicketCounts] = useState({
    child: 0,
    adult: 0,
    senior: 0,
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [bookingSuccess, setBookingSuccess] = useState(false)

  // Fetch movies on mount
  useEffect(() => {
    fetchMovies()
      .then(setMovies)
      .catch((err) => setError(err.message))
  }, [])

  // Placeholder for fetching seats
  useEffect(() => {
    if (!selectedMovieId || !showtime) {
      setLoading(false)
      return
    }
    // Simulate a short loading state for demo purposes
    setLoading(true)
    const t = setTimeout(() => setLoading(false), 300)
    return () => clearTimeout(t)
  }, [selectedMovieId, showtime])

  const selectedMovie = movies.find((m) => m.title === selectedMovieId)
  const showtimes = getShowtimes(selectedMovie)

  const updateTicketCount = (type, delta) => {
    setTicketCounts((current) => {
      const next = Math.max(0, current[type] + delta)
      return { ...current, [type]: next }
    })
  }

  const totalTickets =
    ticketCounts.child + ticketCounts.adult + ticketCounts.senior

  // Demo booking handler (no backend calls)
  const handleBuyTicket = () => {
    setError('')
    setLoading(true)
    setTimeout(() => {
      setLoading(false)
      setBookingSuccess(true)
      setShowtime('')
      setSelectedMovieId('')
      setTimeout(() => {
        setBookingSuccess(false)
        navigate('/')
      }, 2000)
    }, 700)
  }

  return (
    <main className="booking-page">
      <h1 style={{ color: '#000' }}>Booking Page UI</h1>

      {bookingSuccess && (
        <div className="success-message">
          ✓ Booking confirmed! Redirecting...
        </div>
      )}

      {error && <div className="error-message">{error}</div>}

      <div className="booking-container">
        {/* Movie Selection */}
        <div className="selection-group">
          <label htmlFor="movie-select">Select Movie:</label>
          <select
            id="movie-select"
            value={selectedMovieId}
            onChange={(e) => {
              setSelectedMovieId(e.target.value)
              setShowtime('')
            }}
          >
            <option value="">-- Choose a movie --</option>
            {movies.map((movie) => (
              <option key={movie.title} value={movie.title}>
                {movie.title}
              </option>
            ))}
          </select>
        </div>

        {/* Showtime Selection */}
        {selectedMovieId && (
          <div className="selection-group">
            <label htmlFor="showtime-select">Select Showtime:</label>
            <select
              id="showtime-select"
              value={showtime}
              onChange={(e) => setShowtime(e.target.value)}
            >
              <option value="">-- Choose a showtime --</option>
              {showtimes.map((time) => (
                <option key={time} value={time}>
                  {time}
                </option>
              ))}
            </select>
          </div>
        )}

        {/* Selection summary removed - no text under selects for demo */}

        {/* Demo booking UI: no real seats in demo */}
        {selectedMovie && showtime && (
          <div className="seat-selection">
            <h2>{selectedMovie.title} - {showtime}</h2>

            <div className="ticket-types">
              <h3>Choose ticket quantities</h3>
              {['child', 'adult', 'senior'].map((type) => {
                const label = type.charAt(0).toUpperCase() + type.slice(1)
                return (
                  <div key={type} className="ticket-type-row">
                    <div className="ticket-type-label">{label}</div>
                    <div className="ticket-counter">
                      <button
                        type="button"
                        className="counter-button"
                        onClick={() => updateTicketCount(type, -1)}
                        disabled={ticketCounts[type] === 0}
                      >
                        −
                      </button>
                      <span>{ticketCounts[type]}</span>
                      <button
                        type="button"
                        className="counter-button"
                        onClick={() => updateTicketCount(type, 1)}
                      >
                        +
                      </button>
                    </div>
                  </div>
                )
              })}
            </div>

            <div className="screen">SCREEN</div>

            {loading ? (
              <p>Booking Seat</p>
            ) : (
              <div className="demo-note">
                <div className="demo-seats-grid" aria-hidden>
                  {['A', 'B', 'C', 'D'].map((row) => (
                    <div key={row} className="row">
                      {Array.from({ length: 8 }).map((_, i) => {
                        const label = `${row}${i + 1}`
                        // mark some seats as booked for the demo
                        const booked = (row === 'B' && i % 3 === 0) || (row === 'C' && i % 4 === 0)
                        return (
                          <div
                            key={label}
                            className={`seat ${booked ? 'booked' : ''}`}
                            title={`Seat ${label}`}
                          >
                            {label}
                          </div>
                        )
                      })}
                    </div>
                  ))}
                </div>

                <button
                  onClick={handleBuyTicket}
                  className="book-button"
                  disabled={totalTickets === 0}
                >
                  {loading ? 'Processing...' : 'Buy Tickets'}
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </main>
  )
}
