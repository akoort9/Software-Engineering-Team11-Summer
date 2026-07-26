export async function fetchMovies() {
  const res = await fetch('/api/movies')
  if (!res.ok) throw new Error('Failed to load movies')
  return res.json()
}

export async function fetchMovie(id) {
  const movies = await fetchMovies()
  return movies.find((movie) => String(movie.id) === String(id)) ?? null
}

export async function addMovie(adminEmail, movie) {
  const res = await fetch('/api/movies', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ adminEmail, movie }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => null)
    throw new Error(data?.error || 'Failed to save movie')
  }
  return res.json()
}

export async function fetchShowrooms() {
  const res = await fetch('/api/showrooms')
  if (!res.ok) throw new Error('Failed to load showrooms')
  return res.json()
}

export async function scheduleShowtime(adminEmail, { movie, showroomID, startTime, endTime }) {
  const res = await fetch('/api/showtimes', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ adminEmail, movie, showroomID, startTime, endTime }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => null)
    throw new Error(data?.error || 'Failed to schedule showtime')
  }
  return res.json()
}

