import { createContext, useContext, useState } from 'react'
import { setCurrentEmail, clearCurrentEmail } from '../utils/currentUser.js'

const AuthContext = createContext(null)
const STORAGE_KEY = 'currentUser'

function readStoredUser() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    const storedUser = raw ? JSON.parse(raw) : null
    if (storedUser && storedUser.email) {
      // keep currentUser.js in sync for sessions that started before a refresh
      setCurrentEmail(storedUser.email)
    }
    return storedUser
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser)

  const login = (nextUser) => {
    setUser(nextUser)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextUser))
    setCurrentEmail(nextUser.email)
  }

  const logout = () => {
    setUser(null)
    localStorage.removeItem(STORAGE_KEY)
    clearCurrentEmail()
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
