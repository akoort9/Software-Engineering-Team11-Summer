import { getCurrentEmail } from '../utils/currentUser.js'

export async function fetchUser(email = getCurrentEmail()) {
  const res = await fetch(`/api/user?email=${email}`)
  if (!res.ok) throw new Error('Failed to load profile')
  return res.json()
}

export async function updateProfile(profile, email = getCurrentEmail()) {
  const res = await fetch('/api/user', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ...profile, email }),
  })
  if (!res.ok) throw new Error('Failed to save profile')
  return res.json()
}

export async function fetchFavorites(email = getCurrentEmail()) {
  const res = await fetch(`/api/favorites?email=${email}`)
  if (!res.ok) throw new Error('Failed to load favorites')
  return res.json()
}

export async function addFavorite(movieId, email = getCurrentEmail()) {
  const res = await fetch('/api/favorites', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, movieId }),
  })
  if (!res.ok) throw new Error('Failed to add favorite')
  return res.json()
}

export async function removeFavorite(movieId, email = getCurrentEmail()) {
  const res = await fetch('/api/favorites', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, movieId }),
  })
  if (!res.ok) throw new Error('Failed to remove favorite')
  return res.json()
}

export async function fetchCards(email = getCurrentEmail()) {
  const res = await fetch(`/api/cards?email=${email}`)
  if (!res.ok) throw new Error('Failed to load cards')
  return res.json()
}

export async function addCard(card, email = getCurrentEmail()) {
  const res = await fetch('/api/cards', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ...card, email }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.error || 'Failed to add card')
  }
  return res.json()
}
