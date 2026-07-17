import { useCallback, useEffect, useState } from 'react'
import { fetchFavorites, addFavorite, removeFavorite } from '../api/users.js'
import { useAuth } from '../auth/AuthContext.jsx'

export function useFavorites() {
  const { user } = useAuth()
  const [favIds, setFavIds] = useState(() => new Set())

  useEffect(() => {
    if (!user) {
      setFavIds(new Set())
      return
    }
    let active = true
    fetchFavorites()
      .then((movies) => {
        if (active) setFavIds(new Set(movies.map((movie) => movie.id)))
      })
      .catch(() => {})
    return () => {
      active = false
    }
  }, [user])

  const isFavorited = useCallback((id) => favIds.has(id), [favIds])

  const toggleFavorite = useCallback(
    async (id) => {
      if (!user) return
      const wasFavorited = favIds.has(id)

      setFavIds((prev) => {
        const next = new Set(prev)
        if (wasFavorited) next.delete(id)
        else next.add(id)
        return next
      })

      try {
        if (wasFavorited) await removeFavorite(id)
        else await addFavorite(id)
      } catch {
        setFavIds((prev) => {
          const next = new Set(prev)
          if (wasFavorited) next.add(id)
          else next.delete(id)
          return next
        })
      }
    },
    [user, favIds]
  )

  return { isFavorited, toggleFavorite, canFavorite: Boolean(user) }
}
