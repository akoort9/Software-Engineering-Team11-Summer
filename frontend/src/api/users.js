/**
 * Sends a new user to the backend to be stored in the database.
 * @param user the registration payload (name, email, password, etc.)
 * @returns the created user as returned by the API
 */
export async function registerUser(user) {
  const res = await fetch('/api/user', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(user),
  })

  if (!res.ok) {
    let message = 'Failed to create account'
    try {
      const data = await res.json()
      if (data && data.error) message = data.error
    } catch {
      // response had no JSON body; keep the default message
    }
    throw new Error(message)
  }

  return res.json()
}
