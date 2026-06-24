export function embedUrl(url) {
  if (!url) return ''

  const watch = url.match(/[?&]v=([^&]+)/)
  if (watch) return `https://www.youtube.com/embed/${watch[1]}`

  const short = url.match(/youtu\.be\/([^?&]+)/)
  if (short) return `https://www.youtube.com/embed/${short[1]}`

  return url
}
