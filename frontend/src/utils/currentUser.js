const STORAGE_KEY = 'ces:userEmail'

export function getCurrentEmail() {
  return localStorage.getItem(STORAGE_KEY) || ''
}

export function setCurrentEmail(email) {
  localStorage.setItem(STORAGE_KEY, email)
}

export function clearCurrentEmail() {
  localStorage.removeItem(STORAGE_KEY)
}
