// Formats a real Showtime record (from the backend) for display.
export function formatShowtimeLabel(showtime) {
  if (!showtime?.startTime) return ''
  const date = new Date(showtime.startTime)
  return date.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

// Local (not UTC) calendar-day key, e.g. "2026-07-30", used to group/filter showtimes by day.
export function getShowtimeDateKey(showtime) {
  if (!showtime?.startTime) return ''
  return new Date(showtime.startTime).toLocaleDateString('en-CA')
}

// Formats a date key (as produced by getShowtimeDateKey) for display in a filter dropdown.
export function formatDateKeyLabel(dateKey) {
  if (!dateKey) return ''
  const [year, month, day] = dateKey.split('-').map(Number)
  const date = new Date(year, month - 1, day)
  return date.toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}
