export async function fetchMovies() {
  const res = await fetch('/api/movies')
  if (!res.ok) throw new Error('Failed to load movies')
  return res.json()
}

export async function fetchMovie(id) {
  const movies = await fetchMovies()
  return movies.find((movie) => String(movie.id) === String(id)) ?? null
}

export async function addMovie(title) {
  const res = await fetch('/api/movies', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title }),
  })
  if (!res.ok) throw new Error('Failed to save movie')
  return res.json()
}

