package cs4050e.ces.db.payment;

import cs4050e.ces.db.DataHandler;

/** Represents a ticket to one {@code Seat} for a {@code Showtime}. */
public class Ticket {
    /**
     * Tracks the type of the ticket and therefore its price
     * {@code ADULT} - Adult price ticket.
     * {@code CHILD} - Child price ticket.
     * {@code SENIOR} - Senior price ticket.
     */
    public enum TicketType {
        ADULT,
        CHILD,
        SENIOR
    };

    /** Database ID. */
    int ticketId;

    /** Type of ticket to determine price. */
    TicketType ticketType;

    /** Price of the ticket. */
    double price;

    /**
     * Initializes a new {@code Ticket} object.
     * @param id The database ID.
     * @param type The ticket's type, defining its price.
     */
    public Ticket(int id, TicketType type) {
        if (type == null) {
            throw new IllegalArgumentException("type must be a TicketType enum");
        } // if

        this.ticketId = id;
        this.ticketType = type;
        this.price = DataHandler.getTicketPrice(this.ticketType);
    } // Ticket
} // Ticket
