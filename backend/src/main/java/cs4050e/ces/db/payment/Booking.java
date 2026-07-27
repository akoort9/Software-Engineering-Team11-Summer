package cs4050e.ces.db.payment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Represents a {@code User}'s booking for one or more
 * {@code Ticket}s.
 */
public class Booking {

    /**
     * Holds the possible states of a {@code Booking}.
     * {@code PENDING} - The booking is being actively processed by 
     * the system.
     * {@code CONFIRMED} - All details of the booking were completed: 
     * payment, reserving seats, distributing tickets, etc.
     * {@code CANCELLED} - The booking was cancelled due to a 
     * processing error or user choice.
     */
    public enum BookingState {
        PENDING,
        CONFIRMED,
        CANCELLED
    };

    /** Database ID. */
    private int bookingId;

    /** Date and time the booking was created. */
    private LocalDateTime bookingDate;

    /** Total price of the booking, including all ticket prices. */
    private double totalPrice = 0;

    /** Current state of the booking. */
    private BookingState state;

    /** Tickets included in the booking. */
    private List<Ticket> tickets;

    public Booking(int id) {
        this.bookingId = id;
        this.bookingDate = LocalDateTime.now();
        this.state = BookingState.PENDING;
        this.tickets = new ArrayList<Ticket>();
    } // Booking

    public boolean addTicket(Ticket ticket) {
        if (ticket == null) {
            return false;
        } // if

        return tickets.add(ticket);
    } // addTicket

    public void cancelBooking() {
        this.state = BookingState.CANCELLED;
    } // cancelBooking

    public double calculateTotal(Promotion promo) {
        // add all tickets
        for (int i = 0; i < this.tickets.size(); i++) {
            totalPrice += this.tickets.get(i).getPrice();
        } // for

        // add discount
        if (promo != null) {
            totalPrice *= promo.getDiscountPercent();
        } // if
       
        return totalPrice;
    } // calculateTotal
} // Booking
