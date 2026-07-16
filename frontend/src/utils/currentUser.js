// Fallback until login is wired up; login should call setCurrentEmail().
export const DEMO_EMAIL = 'johndaboss@epic.com'

const STORAGE_KEY = 'ces:userEmail'

export function getCurrentEmail() {
  return localStorage.getItem(STORAGE_KEY) || DEMO_EMAIL
}

export function setCurrentEmail(email) {
  localStorage.setItem(STORAGE_KEY, email)
}

export function clearCurrentEmail() {
  localStorage.removeItem(STORAGE_KEY)
}
