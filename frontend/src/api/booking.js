import { getCurrentEmail } from '../utils/currentUser.js'

export async function fetchAllShowtimes() {
  const res = await fetch('/api/showtimes')
  if (!res.ok) throw new Error('Failed to load showtimes')
  return res.json()
}

export async function fetchShowtimesForMovie(movieId) {
  const res = await fetch(`/api/showtimes?movieId=${encodeURIComponent(movieId)}`)
  if (!res.ok) throw new Error('Failed to load showtimes')
  return res.json()
}

export async function fetchSeats(showtimeId) {
  const res = await fetch(`/api/seats?showtimeId=${encodeURIComponent(showtimeId)}`)
  if (!res.ok) throw new Error('Failed to load seats')
  return res.json()
}

export async function bookTickets({ showtimeId, seats, email = getCurrentEmail() }) {
  const res = await fetch('/api/bookings', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, showtimeId, seats }),
  })

  const data = await res.json().catch(() => ({}))

  if (!res.ok) {
    const error = new Error(data.error || 'Failed to book tickets')
    error.status = res.status
    error.conflictSeatIds = data.seatIds || []
    throw error
  }

  return data
}
