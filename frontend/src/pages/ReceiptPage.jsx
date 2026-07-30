import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import '../styles/ReceiptPage.css'

const TICKET_TYPE_LABEL = {
  standard: 'Adult',
  child: 'Child',
  senior: 'Senior',
}

function ticketTypeLabel(type) {
  return TICKET_TYPE_LABEL[type?.toLowerCase()] ?? type
}

export default function ReceiptPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const receipt = location.state

  // Reaching this page any way other than a fresh redirect from checkout
  // (e.g. a refresh) leaves no receipt to show, so send the user home.
  if (!receipt) {
    return <Navigate to="/" replace />
  }

  const {
    movieTitle,
    showtimeLabel,
    tickets = [],
    totalPrice = 0,
    transactionId,
    cardLastFour,
  } = receipt

  return (
    <main className="receipt-page">
      <div className="receipt-card">
        <h1>Thank You for Your Order!</h1>
        <p className="receipt-subtitle">A confirmation email with your tickets is on its way.</p>

        {(movieTitle || showtimeLabel || transactionId) && (
          <div className="receipt-section">
            {movieTitle && (
              <div>
                <strong>Movie:</strong> {movieTitle}
              </div>
            )}
            {showtimeLabel && (
              <div>
                <strong>Showtime:</strong> {showtimeLabel}
              </div>
            )}
            {transactionId && (
              <div>
                <strong>Transaction:</strong> {transactionId}
              </div>
            )}
          </div>
        )}

        <div className="receipt-section">
          <div className="order-number">
            <span className="order-number-label">Order Number</span>
            <span className="order-number-value">67</span>
          </div>
        </div>

        <div className="receipt-section">
          <h2>Order Summary</h2>
          <ul className="receipt-items">
            {tickets.map((ticket) => (
              <li key={ticket.id ?? `${ticket.seatId}-${ticket.ticketType}`} className="receipt-item">
                <span>
                  Seat {ticket.seatLabel} - {ticketTypeLabel(ticket.ticketType)}
                </span>
                <span>${ticket.price.toFixed(2)}</span>
              </li>
            ))}
          </ul>
          <div className="receipt-total">
            <span>Total</span>
            <span>${totalPrice.toFixed(2)}</span>
          </div>
        </div>

        <div className="receipt-section">
          <div>
            <strong>Payment Method:</strong> Card ending in {cardLastFour ?? '----'}
          </div>
        </div>

        <div className="receipt-actions">
          <button type="button" className="receipt-button" onClick={() => window.print()}>
            Print Receipt
          </button>
          <button
            type="button"
            className="receipt-button secondary"
            onClick={() => navigate('/')}
          >
            Return to Home
          </button>
        </div>
      </div>
    </main>
  )
}
