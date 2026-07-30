export async function fetchUsers(adminEmail) {
  const res = await fetch(`/api/users?email=${encodeURIComponent(adminEmail)}`)
  if (!res.ok) throw new Error('Failed to load users')
  return res.json()
}

export async function setUserState(adminEmail, email, state) {
  const res = await fetch('/api/users', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ adminEmail, email, state }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => null)
    throw new Error(data?.error || 'Failed to update user')
  }
  return res.json()
}

export async function fetchPromotions(adminEmail) {
  const res = await fetch(`/api/promotions?email=${encodeURIComponent(adminEmail)}`)
  if (!res.ok) throw new Error('Failed to load promotions')
  return res.json()
}

export async function createPromotion(adminEmail, { promoCode, percentOff, expirationDate }) {
  const res = await fetch('/api/promotions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ adminEmail, promoCode, percentOff, expirationDate }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => null)
    throw new Error(data?.error || 'Failed to create promotion')
  }
  return res.json()
}
