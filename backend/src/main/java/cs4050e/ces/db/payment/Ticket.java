package cs4050e.ces.db.payment;

import java.sql.Date;

import cs4050e.ces.db.DataHandler;

/** Represents a ticket to one {@code Seat} for a {@code Showtime}. */
public class Ticket {
    /** Connection to the database. */
    private static final DataHandler db = DataHandler.getInstance();

    /**
     * Tracks the class of the ticket and therefore its price.
     * {@code STANDARD} - Standard price ticket.
     * {@code CHILD} - Child price ticket.
     * {@code SENIOR} - Senior price ticket.
     */
    public enum TicketType {
        STANDARD,
        CHILD,
        SENIOR
    };

    /** Database ID. */
    private int id = -1;

    /** The user who purchased this ticket. */
    private int userId;

    /** The showtime this ticket is for. */
    private int showtimeId;

    /** The seat reserved by this ticket. */
    private int seatId;

    /** Class of ticket, determines price. */
    private TicketType ticketClass;

    /** Price of the ticket. */
    private double price;

    /** When the ticket was purchased. */
    private Date purchaseDate;

    /**
     * Initializes a new {@code Ticket} object, looking up its price
     * from the database based on {@code ticketClass}.
     * @param userId The ID of the purchasing user.
     * @param showtimeId The ID of the showtime being booked.
     * @param seatId The ID of the seat being reserved.
     * @param ticketClass The ticket's class, defining its price.
     * @param date The date of purchase.
     */
    public Ticket(int userId, int showtimeId, int seatId, TicketType ticketClass, Date date) {
        if (ticketClass == null) {
            throw new IllegalArgumentException("ticketClass must be a TicketType enum");
        } // if

        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seatId = seatId;
        this.ticketClass = ticketClass;
        this.price = db.getTicketPrice(this.ticketClass);
        this.purchaseDate = date;
    } // Ticket

    public int getId() {
        return id;
    } // getId

    public void setId(int id) {
        this.id = id;
    } // setId

    public int getUserId() {
        return userId;
    } // getUserId

    public int getShowtimeId() {
        return showtimeId;
    } // getShowtimeId

    public int getSeatId() {
        return seatId;
    } // getSeatId

    public TicketType getTicketClass() {
        return ticketClass;
    } // getTicketClass

    public String getTypeString() {
        switch (this.ticketClass) {
            case STANDARD:
                return "STANDARD";
            case CHILD:
                return "CHILD";
            case SENIOR:
                return "SENIOR";
            default:
                return "";
        } // switch
    } // getTypeString

    public double getPrice() {
        return price;
    } // getPrice

    public Date getPurchaseDate() {
        return purchaseDate;
    } // getPurchaseDate
} // Ticket
