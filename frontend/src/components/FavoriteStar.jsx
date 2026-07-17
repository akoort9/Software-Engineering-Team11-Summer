export default function FavoriteStar({ favorited, onClick, className = '' }) {
  return (
    <button
      type="button"
      className={`favorite-star ${favorited ? 'is-favorited' : ''} ${className}`.trim()}
      onClick={onClick}
      aria-pressed={favorited}
      aria-label={favorited ? 'Remove from favorites' : 'Add to favorites'}
      title={favorited ? 'Remove from favorites' : 'Add to favorites'}
    >
      {favorited ? '★' : '☆'}
    </button>
  )
}
