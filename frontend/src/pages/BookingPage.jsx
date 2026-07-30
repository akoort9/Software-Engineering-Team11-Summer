import { useEffect, useMemo, useState } from 'react'
import { useSearchParams, useNavigate, Link } from 'react-router-dom'
import { fetchMovies } from '../api/movies.js'
import { fetchShowtimesForMovie, fetchSeats, bookTickets } from '../api/booking.js'
import { fetchCards } from '../api/users.js'
import { formatShowtimeLabel } from '../utils/showtimes.js'
import { useAuth } from '../auth/AuthContext.jsx'
import '../styles/BookingPage.css'

const EMPTY_NEW_CARD = {
  cardNumber: '',
  expirationDate: '',
  cvv: '',
  billingAddress: '',
}

const TICKET_PRICES = {
  child: 8,
  adult: 12,
  senior: 10,
}

// The backend only knows "standard" ticket pricing; "adult" is just the
// customer-facing label for it.
const TICKET_TYPE_FOR_API = {
  child: 'child',
  adult: 'standard',
  senior: 'senior',
}

const TICKET_TYPE_LABEL = {
  child: 'Child',
  adult: 'Adult',
  senior: 'Senior',
}

function reservationKey(showtimeId) {
  return `seat-reservation:${showtimeId}`
}

function readReservedSeats(showtimeId) {
  try {
    const raw = sessionStorage.getItem(reservationKey(showtimeId))
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

// Zips the selected seats (in the order they were clicked) with a flattened
// list of ticket types built from the counters, so every selected seat ends
// up with a ticket class.
function assignSeatTickets(selectedSeatIds, ticketCounts, seatsById) {
  const order = []
  for (let i = 0; i < ticketCounts.child; i++) order.push('child')
  for (let i = 0; i < ticketCounts.adult; i++) order.push('adult')
  for (let i = 0; i < ticketCounts.senior; i++) order.push('senior')

  return selectedSeatIds.map((seatId, i) => ({
    seatId,
    label: seatsById.get(seatId)?.label ?? '?',
    type: order[i],
  }))
}

export default function BookingPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { user } = useAuth()

  const [movies, setMovies] = useState([])
  const [selectedMovieId, setSelectedMovieId] = useState(searchParams.get('movieId') || '')
  const [showtimes, setShowtimes] = useState([])
  const [selectedShowtimeId, setSelectedShowtimeId] = useState(searchParams.get('showtimeId') || '')
  const [ticketCounts, setTicketCounts] = useState({ child: 0, adult: 0, senior: 0 })
  const [seats, setSeats] = useState([])
  const [selectedSeatIds, setSelectedSeatIds] = useState([])

  const [step, setStep] = useState('select') // 'select' | 'summary'
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [savedCards, setSavedCards] = useState([])
  const [paymentMode, setPaymentMode] = useState('new') // 'saved' | 'new'
  const [selectedCardId, setSelectedCardId] = useState('')
  const [newCard, setNewCard] = useState(EMPTY_NEW_CARD)
  const [saveCard, setSaveCard] = useState(false)

  const totalTickets = ticketCounts.child + ticketCounts.adult + ticketCounts.senior
  const canConfirmBooking = totalTickets > 0 && selectedSeatIds.length === totalTickets

  const seatsById = useMemo(() => new Map(seats.map((seat) => [seat.id, seat])), [seats])
  const seatRows = useMemo(() => {
    const rows = new Map()
    for (const seat of seats) {
      if (!rows.has(seat.rowLabel)) rows.set(seat.rowLabel, [])
      rows.get(seat.rowLabel).push(seat)
    }
    for (const row of rows.values()) row.sort((a, b) => a.seatNumber - b.seatNumber)
    return [...rows.entries()].sort(([a], [b]) => a.localeCompare(b))
  }, [seats])

  const selectedMovie = movies.find((m) => String(m.id) === String(selectedMovieId))
  const selectedShowtime = showtimes.find((st) => String(st.id) === String(selectedShowtimeId))

  // load movies once
  useEffect(() => {
    fetchMovies()
      .then(setMovies)
      .catch((err) => setError(err.message))
  }, [])

  // load the customer's saved cards so they can pay with one at checkout
  useEffect(() => {
    if (!user) {
      setSavedCards([])
      return
    }
    fetchCards(user.email)
      .then((cards) => {
        setSavedCards(cards)
        if (cards.length > 0) {
          setPaymentMode('saved')
          setSelectedCardId(String(cards[0].id))
        }
      })
      .catch(() => {})
  }, [user])

  // load showtimes whenever the selected movie changes
  useEffect(() => {
    if (!selectedMovieId) {
      setShowtimes([])
      return
    }
    fetchShowtimesForMovie(selectedMovieId)
      .then((data) => {
        setShowtimes(data)
        setSelectedShowtimeId((current) =>
          data.some((st) => String(st.id) === String(current)) ? current : ''
        )
      })
      .catch((err) => setError(err.message))
  }, [selectedMovieId])

  // load seats whenever the selected showtime changes, and restore this
  // session's in-progress seat selection for that showtime, if any
  useEffect(() => {
    if (!selectedShowtimeId) {
      setSeats([])
      setSelectedSeatIds([])
      return
    }

    setLoading(true)
    fetchSeats(selectedShowtimeId)
      .then((data) => {
        setSeats(data)

        const bookedIds = new Set(data.filter((s) => s.booked).map((s) => s.id))
        const restored = readReservedSeats(selectedShowtimeId)
        setSelectedSeatIds(restored.filter((id) => !bookedIds.has(id)))
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [selectedShowtimeId])

  // keep the selection in sync with the ticket counts
  useEffect(() => {
    setSelectedSeatIds((current) => {
      if (totalTickets === 0) return []
      if (current.length > totalTickets) return current.slice(0, totalTickets)
      return current
    })
  }, [totalTickets])

  // persist this session's in-progress seat selection
  useEffect(() => {
    if (!selectedShowtimeId) return
    sessionStorage.setItem(reservationKey(selectedShowtimeId), JSON.stringify(selectedSeatIds))
  }, [selectedShowtimeId, selectedSeatIds])

  const handleToggleSeat = (seat) => {
    if (seat.booked) return
    setSelectedSeatIds((current) => {
      if (current.includes(seat.id)) {
        return current.filter((id) => id !== seat.id)
      }
      if (current.length >= totalTickets) return current
      return [...current, seat.id]
    })
  }

  const updateTicketCount = (type, delta) => {
    setTicketCounts((current) => ({
      ...current,
      [type]: Math.max(0, current[type] + delta),
    }))
  }

  const totalCost =
    ticketCounts.child * TICKET_PRICES.child +
    ticketCounts.adult * TICKET_PRICES.adult +
    ticketCounts.senior * TICKET_PRICES.senior

  const seatAssignments = useMemo(
    () => assignSeatTickets(selectedSeatIds, ticketCounts, seatsById),
    [selectedSeatIds, ticketCounts, seatsById]
  )

  const handleProceedToCheckout = () => {
    if (!canConfirmBooking) return
    setError('')
    setStep('summary')
  }

  const updateNewCardField = (e) =>
    setNewCard({ ...newCard, [e.target.name]: e.target.value })

  const validatePayment = () => {
    if (paymentMode === 'saved') {
      return selectedCardId ? '' : 'Please choose a saved card.'
    }
    const number = newCard.cardNumber.replace(/\s+/g, '')
    if (!/^\d{13,19}$/.test(number)) return 'Enter a valid card number (13-19 digits).'
    if (!/^\d{4}-\d{2}$/.test(newCard.expirationDate)) return 'Enter an expiration date (YYYY-MM).'
    if (!/^\d{3,4}$/.test(newCard.cvv)) return 'Enter a valid security code (3-4 digits).'
    return ''
  }

  const buildPayment = () => {
    if (paymentMode === 'saved') {
      return { cardId: Number(selectedCardId) }
    }
    return {
      cardNumber: newCard.cardNumber.replace(/\s+/g, ''),
      expirationDate: newCard.expirationDate,
      cvv: newCard.cvv,
      billingAddress: newCard.billingAddress,
      saveCard,
    }
  }

  const handleConfirmBooking = async () => {
    setError('')

    const paymentError = validatePayment()
    if (paymentError) {
      setError(paymentError)
      return
    }

    setLoading(true)
    try {
      const result = await bookTickets({
        showtimeId: Number(selectedShowtimeId),
        seats: seatAssignments.map((a) => ({
          seatId: a.seatId,
          ticketType: TICKET_TYPE_FOR_API[a.type],
        })),
        payment: buildPayment(),
      })

      sessionStorage.removeItem(reservationKey(selectedShowtimeId))
      navigate('/receipt', {
        state: {
          movieTitle: selectedMovie?.title,
          showtimeLabel: selectedShowtime ? formatShowtimeLabel(selectedShowtime) : '',
          tickets: result.tickets,
          totalPrice: result.totalPrice,
          transactionId: result.transactionId,
          cardLastFour: result.cardLastFour,
          confirmationNumber: result.confirmationNumber,
        },
      })
    } catch (err) {
      if (err.status === 409) {
        setError(
          'Some of your selected seats were just booked by someone else. Please choose different seats.'
        )
        setStep('select')
        try {
          const refreshed = await fetchSeats(selectedShowtimeId)
          setSeats(refreshed)
          setSelectedSeatIds((current) =>
            current.filter((id) => !err.conflictSeatIds.includes(id))
          )
        } catch {
          // keep whatever seat data we already have
        }
      } else {
        // payment declines (402) and other errors keep the user on checkout to retry
        setError(err.message)
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="booking-page">
      <h1>Book Tickets</h1>

      {error && <div className="error-message">{error}</div>}

      <div className="booking-container">
          <div className="selection-group">
            <label htmlFor="movie-select">Select Movie:</label>
            <select
              id="movie-select"
              value={selectedMovieId}
              disabled={step === 'summary'}
              onChange={(e) => {
                setSelectedMovieId(e.target.value)
                setSelectedShowtimeId('')
                setTicketCounts({ child: 0, adult: 0, senior: 0 })
                setSelectedSeatIds([])
              }}
            >
              <option value="">-- Choose a movie --</option>
              {movies.map((movie) => (
                <option key={movie.id} value={movie.id}>
                  {movie.title}
                </option>
              ))}
            </select>
          </div>

          {selectedMovieId && (
            <div className="selection-group">
              <label htmlFor="showtime-select">Select Showtime:</label>
              <select
                id="showtime-select"
                value={selectedShowtimeId}
                disabled={step === 'summary'}
                onChange={(e) => setSelectedShowtimeId(e.target.value)}
              >
                <option value="">-- Choose a showtime --</option>
                {showtimes.map((st) => (
                  <option key={st.id} value={st.id}>
                    {formatShowtimeLabel(st)}
                  </option>
                ))}
              </select>
              {showtimes.length === 0 && <p>No showtimes scheduled for this movie.</p>}
            </div>
          )}

          {selectedMovie && selectedShowtime && step === 'select' && (
            <div className="seat-selection">
              <h2>
                {selectedMovie.title} - {formatShowtimeLabel(selectedShowtime)}
              </h2>

              <div className="ticket-types">
                <h3>Choose ticket quantities</h3>
                {['child', 'adult', 'senior'].map((type) => (
                  <div key={type} className="ticket-type-row">
                    <div className="ticket-type-details">
                      <div className="ticket-type-label">{TICKET_TYPE_LABEL[type]}</div>
                      <div className="ticket-price">${TICKET_PRICES[type].toFixed(2)} each</div>
                    </div>
                    <div className="ticket-counter">
                      <button
                        type="button"
                        className="counter-button"
                        onClick={() => updateTicketCount(type, -1)}
                        disabled={ticketCounts[type] === 0}
                      >
                        −
                      </button>
                      <span>{ticketCounts[type]}</span>
                      <button
                        type="button"
                        className="counter-button"
                        onClick={() => updateTicketCount(type, 1)}
                      >
                        +
                      </button>
                    </div>
                  </div>
                ))}
              </div>

              <div className="screen">SCREEN</div>

              <div className="seat-instructions">
                {totalTickets === 0
                  ? 'Choose ticket quantities to start selecting seats.'
                  : `Select ${totalTickets} seat${totalTickets === 1 ? '' : 's'} (${selectedSeatIds.length}/${totalTickets})`}
              </div>

              {loading ? (
                <p>Loading seats...</p>
              ) : (
                <>
                  <div className="demo-seats-grid">
                    {seatRows.map(([rowLabel, rowSeats]) => (
                      <div key={rowLabel} className="row">
                        {rowSeats.map((seat) => {
                          const selected = selectedSeatIds.includes(seat.id)
                          return (
                            <button
                              key={seat.id}
                              type="button"
                              className={`seat ${seat.booked ? 'booked' : selected ? 'selected' : ''}`}
                              title={`Seat ${seat.label}`}
                              disabled={seat.booked || totalTickets === 0}
                              onClick={() => handleToggleSeat(seat)}
                              aria-pressed={selected}
                            >
                              {seat.label}
                            </button>
                          )
                        })}
                      </div>
                    ))}
                  </div>

                  <div className="seat-legend">
                    <div className="legend-item">
                      <button type="button" className="seat" disabled />
                      Available
                    </div>
                    <div className="legend-item">
                      <button type="button" className="seat selected" disabled />
                      Selected
                    </div>
                    <div className="legend-item">
                      <button type="button" className="seat booked" disabled />
                      Booked
                    </div>
                  </div>

                  <div className="purchase-summary">
                    <div className="total-cost">Total: ${totalCost.toFixed(2)}</div>
                    <button
                      onClick={handleProceedToCheckout}
                      className="book-button"
                      disabled={!canConfirmBooking || !user}
                    >
                      Proceed to Checkout
                    </button>
                  </div>
                  {!user && <p>Please <Link to="/login">log in</Link> to book tickets.</p>}
                </>
              )}
            </div>
          )}

          {step === 'summary' && selectedMovie && selectedShowtime && (
            <div className="seat-selection">
              <h2>Order Summary</h2>

              <div className="booking-summary">
                <div><strong>Movie:</strong> {selectedMovie.title}</div>
                <div><strong>Showtime:</strong> {formatShowtimeLabel(selectedShowtime)}</div>
                <div>
                  <strong>Seats:</strong>{' '}
                  {seatAssignments
                    .map((a) => `${a.label} (${TICKET_TYPE_LABEL[a.type]})`)
                    .join(', ')}
                </div>

                <ul>
                  {seatAssignments.map((a) => (
                    <li key={a.seatId}>
                      Seat {a.label} - {TICKET_TYPE_LABEL[a.type]} - $
                      {TICKET_PRICES[a.type].toFixed(2)}
                    </li>
                  ))}
                </ul>

                <div className="total-price">Total: ${totalCost.toFixed(2)}</div>
              </div>

              <div className="payment-section">
                <h3>Payment</h3>

                {savedCards.length > 0 && (
                  <label className="payment-option">
                    <input
                      type="radio"
                      name="paymentMode"
                      checked={paymentMode === 'saved'}
                      onChange={() => setPaymentMode('saved')}
                    />
                    Use a saved card
                  </label>
                )}

                {paymentMode === 'saved' && savedCards.length > 0 && (
                  <select
                    className="payment-input"
                    value={selectedCardId}
                    onChange={(e) => setSelectedCardId(e.target.value)}
                  >
                    {savedCards.map((card) => (
                      <option key={card.id} value={card.id}>
                        •••• {card.cardNumber.slice(-4)} (exp {card.expirationDate})
                      </option>
                    ))}
                  </select>
                )}

                <label className="payment-option">
                  <input
                    type="radio"
                    name="paymentMode"
                    checked={paymentMode === 'new'}
                    onChange={() => setPaymentMode('new')}
                  />
                  Use a new card
                </label>

                {paymentMode === 'new' && (
                  <div className="new-card-fields">
                    <input
                      className="payment-input"
                      name="cardNumber"
                      placeholder="Card number"
                      value={newCard.cardNumber}
                      onChange={updateNewCardField}
                    />
                    <div className="card-row">
                      <input
                        className="payment-input"
                        name="expirationDate"
                        type="month"
                        placeholder="YYYY-MM"
                        value={newCard.expirationDate}
                        onChange={updateNewCardField}
                      />
                      <input
                        className="payment-input"
                        name="cvv"
                        placeholder="CVV"
                        value={newCard.cvv}
                        onChange={updateNewCardField}
                      />
                    </div>
                    <input
                      className="payment-input"
                      name="billingAddress"
                      placeholder="Billing address"
                      value={newCard.billingAddress}
                      onChange={updateNewCardField}
                    />
                    <label className="payment-option">
                      <input
                        type="checkbox"
                        checked={saveCard}
                        onChange={(e) => setSaveCard(e.target.checked)}
                      />
                      Save this card to my profile
                    </label>
                  </div>
                )}
              </div>

              <div className="purchase-summary">
                <button
                  type="button"
                  className="book-button"
                  onClick={() => setStep('select')}
                  disabled={loading}
                >
                  Back
                </button>
                <button
                  type="button"
                  className="book-button"
                  onClick={handleConfirmBooking}
                  disabled={loading}
                >
                  {loading ? 'Confirming...' : 'Confirm Booking'}
                </button>
              </div>
            </div>
          )}
        </div>
    </main>
  )
}
