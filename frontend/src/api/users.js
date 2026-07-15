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

export async function loginUser(credentials) {
  const res = await fetch('/api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(credentials),
  })

  if (!res.ok) {
    let message = 'Failed to log in'
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

export async function verifyUser({ email, code }) {
  const res = await fetch('/api/verify', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, code }),
  })

  if (!res.ok) {
    let message = 'Failed to verify account'
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
