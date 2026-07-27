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
