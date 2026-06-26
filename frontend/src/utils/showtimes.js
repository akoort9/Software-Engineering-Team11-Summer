// Placeholder times until the backend serves real per-movie showtimes.
export const PLACEHOLDER_SHOWTIMES = ['2:00 PM', '5:00 PM', '8:00 PM']

export function parseShowtimes(showtimes) {
  if (!showtimes) return []
  return showtimes
    .split(';')
    .flatMap((group) => group.split(','))
    .map((time) => time.trim())
    .filter(Boolean)
}

export function getShowtimes(movie) {
  const parsed = parseShowtimes(movie?.showtimes)
  return parsed.length > 0 ? parsed : PLACEHOLDER_SHOWTIMES
}
