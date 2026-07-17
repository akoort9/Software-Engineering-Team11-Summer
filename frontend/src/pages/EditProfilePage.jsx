import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  fetchUser,
  updateProfile,
  fetchFavorites,
  removeFavorite,
  fetchCards,
  addCard,
  updateCard,
  removeCard,
} from '../api/users.js'
import { getCurrentEmail } from '../utils/currentUser.js'
import FavoriteStar from '../components/FavoriteStar.jsx'
import '../App.css'
import '../styles/AuthPage.css'

export default function EditProfilePage() {
  const email = getCurrentEmail()

  const [profile, setProfile] = useState({
    name: '',
    lastName: '',
    email: '',
    mailingAddress: '',
    state: '',
  })
  const [favorites, setFavorites] = useState([])
  const [cards, setCards] = useState([])
  const [newCard, setNewCard] = useState({
    cardNumber: '',
    billingAddress: '',
    expirationDate: '',
  })
  const [editingCardId, setEditingCardId] = useState(null)
  const [editCard, setEditCard] = useState({
    cardNumber: '',
    billingAddress: '',
    expirationDate: '',
  })
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!email) {
      setLoading(false)
      return
    }
    Promise.all([
      fetchUser(email).then((u) =>
        setProfile({
          name: u.name || '',
          lastName: u.lastName || '',
          email: u.email || '',
          mailingAddress: u.mailingAddress || '',
          state: u.state || '',
        })
      ),
      fetchFavorites(email).then(setFavorites),
      fetchCards(email).then(setCards),
    ])
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [email])

  const updateField = (e) =>
    setProfile({ ...profile, [e.target.name]: e.target.value })
  const updateCardField = (e) =>
    setNewCard({ ...newCard, [e.target.name]: e.target.value })

  const saveProfile = async (e) => {
    e.preventDefault()
    setError('')
    setStatus('')
    try {
      await updateProfile(
        {
          name: profile.name,
          lastName: profile.lastName,
          mailingAddress: profile.mailingAddress,
        },
        email
      )
      setStatus('Profile saved.')
    } catch (err) {
      setError(err.message)
    }
  }

  const handleRemoveFavorite = async (movieId) => {
    setError('')
    try {
      await removeFavorite(movieId, email)
      setFavorites(favorites.filter((movie) => movie.id !== movieId))
    } catch (err) {
      setError(err.message)
    }
  }

  const handleAddCard = async (e) => {
    e.preventDefault()
    setError('')
    setStatus('')

    const cardNumber = newCard.cardNumber.replace(/\s+/g, '')
    if (!/^\d{13,19}$/.test(cardNumber)) {
      setError('Enter a valid card number (13-19 digits).')
      return
    }
    if (!/^\d{4}-\d{2}$/.test(newCard.expirationDate)) {
      setError('Please choose an expiration month (YYYY-MM).')
      return
    }

    try {
      await addCard({ ...newCard, cardNumber }, email)
      setNewCard({ cardNumber: '', billingAddress: '', expirationDate: '' })
      setCards(await fetchCards(email))
      setStatus('Card added.')
    } catch (err) {
      setError(err.message)
    }
  }

  const updateEditCardField = (e) =>
    setEditCard({ ...editCard, [e.target.name]: e.target.value })

  const startEditCard = (card) => {
    setError('')
    setStatus('')
    setEditingCardId(card.id)
    setEditCard({
      cardNumber: card.cardNumber,
      billingAddress: card.billingAddress,
      expirationDate: card.expirationDate,
    })
  }

  const handleUpdateCard = async (e) => {
    e.preventDefault()
    setError('')
    setStatus('')

    const cardNumber = editCard.cardNumber.replace(/\s+/g, '')
    if (!/^\d{13,19}$/.test(cardNumber)) {
      setError('Enter a valid card number (13-19 digits).')
      return
    }
    if (!/^\d{4}-\d{2}$/.test(editCard.expirationDate)) {
      setError('Please choose an expiration month (YYYY-MM).')
      return
    }

    try {
      await updateCard(editingCardId, { ...editCard, cardNumber }, email)
      setEditingCardId(null)
      setCards(await fetchCards(email))
      setStatus('Card updated.')
    } catch (err) {
      setError(err.message)
    }
  }

  const handleRemoveCard = async (cardId) => {
    setError('')
    setStatus('')
    try {
      await removeCard(cardId, email)
      setCards(cards.filter((card) => card.id !== cardId))
      setStatus('Card removed.')
    } catch (err) {
      setError(err.message)
    }
  }

  if (!email) {
    return (
      <main className="auth-page">
        <div className="auth-card">
          <h1>Edit Profile</h1>
          <p className="auth-message">Please log in to view your profile.</p>
          <Link to="/login" className="auth-button">Go to Login</Link>
        </div>
      </main>
    )
  }

  if (loading) {
    return (
      <main className="auth-page">
        <div className="auth-card">
          <p>Loading...</p>
        </div>
      </main>
    )
  }

  return (
    <main className="auth-page">
      <div className="auth-card profile-card">
        <h1>Edit Profile</h1>
        <p className="profile-signed-in">
          Signed in as <strong>{email}</strong>
        </p>

        {status && <div className="profile-alert success">{status}</div>}
        {error && <div className="profile-alert error">{error}</div>}

        <form className="auth-form" onSubmit={saveProfile}>
          <div className="form-group">
            <label htmlFor="name">First Name</label>
            <input id="name" name="name" value={profile.name} onChange={updateField} />
          </div>

          <div className="form-group">
            <label htmlFor="lastName">Last Name</label>
            <input id="lastName" name="lastName" value={profile.lastName} onChange={updateField} />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input id="email" value={profile.email} disabled />
            <small className="field-hint">Email cannot be changed.</small>
          </div>

          <div className="form-group">
            <label htmlFor="mailingAddress">Mailing Address</label>
            <input
              id="mailingAddress"
              name="mailingAddress"
              value={profile.mailingAddress}
              onChange={updateField}
            />
          </div>

          <div className="form-group">
            <label htmlFor="state">Account Status</label>
            <input id="state" value={profile.state} disabled />
          </div>

          <button type="submit" className="auth-button">
            Save Changes
          </button>
        </form>

        <section className="profile-section">
          <h2>Favorite Movies</h2>
          {favorites.length === 0 ? (
            <p className="field-hint">No favorites yet. Add some from a movie's page.</p>
          ) : (
            <ul className="profile-list">
              {favorites.map((movie) => (
                <li key={movie.id} className="profile-list-row">
                  <Link to={`/movies/${movie.id}`}>{movie.title}</Link>
                  <FavoriteStar
                    favorited
                    onClick={() => handleRemoveFavorite(movie.id)}
                  />
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="profile-section">
          <h2>Payment Cards</h2>
          <p className="field-hint">You can store up to 3 cards.</p>
          {cards.length === 0 ? (
            <p className="field-hint">No cards saved.</p>
          ) : (
            <ul className="profile-list">
              {cards.map((card) => (
                <li key={card.id} className="profile-list-row">
                  {editingCardId === card.id ? (
                    <form className="card-edit-form" onSubmit={handleUpdateCard}>
                      <input
                        name="cardNumber"
                        value={editCard.cardNumber}
                        onChange={updateEditCardField}
                        placeholder="Card number"
                      />
                      <input
                        name="billingAddress"
                        value={editCard.billingAddress}
                        onChange={updateEditCardField}
                        placeholder="Billing address"
                      />
                      <input
                        name="expirationDate"
                        type="month"
                        placeholder="YYYY-MM"
                        pattern="\d{4}-\d{2}"
                        value={editCard.expirationDate}
                        onChange={updateEditCardField}
                      />
                      <div className="card-edit-actions">
                        <button type="submit" className="btn-small primary">
                          Save
                        </button>
                        <button
                          type="button"
                          className="btn-small"
                          onClick={() => setEditingCardId(null)}
                        >
                          Cancel
                        </button>
                      </div>
                    </form>
                  ) : (
                    <>
                      <span className="mono">
                        &bull;&bull;&bull;&bull; {card.cardNumber.slice(-4)}
                      </span>
                      <span className="card-row-actions">
                        <span className="field-hint">exp {card.expirationDate}</span>
                        <button
                          type="button"
                          className="link-action"
                          onClick={() => startEditCard(card)}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          className="link-danger"
                          onClick={() => handleRemoveCard(card.id)}
                        >
                          Remove
                        </button>
                      </span>
                    </>
                  )}
                </li>
              ))}
            </ul>
          )}

          {cards.length < 3 && (
            <form className="auth-form card-form" onSubmit={handleAddCard}>
              <div className="form-group">
                <label htmlFor="cardNumber">Card Number</label>
                <input
                  id="cardNumber"
                  name="cardNumber"
                  value={newCard.cardNumber}
                  onChange={updateCardField}
                  placeholder="4242424242424242"
                />
              </div>
              <div className="form-group">
                <label htmlFor="billingAddress">Billing Address</label>
                <input
                  id="billingAddress"
                  name="billingAddress"
                  value={newCard.billingAddress}
                  onChange={updateCardField}
                />
              </div>
              <div className="form-group">
                <label htmlFor="expirationDate">Expiration</label>
                <input
                  id="expirationDate"
                  name="expirationDate"
                  type="month"
                  placeholder="YYYY-MM"
                  pattern="\d{4}-\d{2}"
                  value={newCard.expirationDate}
                  onChange={updateCardField}
                />
                <small className="field-hint">Format: YYYY-MM (e.g. 2027-05)</small>
              </div>
              <button type="submit" className="auth-button">
                Add Card
              </button>
            </form>
          )}
        </section>

        <p className="profile-back">
          <Link to="/">Back to home</Link>
        </p>
      </div>
    </main>
  )
}
