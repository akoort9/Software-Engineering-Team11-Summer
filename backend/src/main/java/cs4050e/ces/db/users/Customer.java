package cs4050e.ces.db.users;

import cs4050e.ces.db.payment.Card;
import cs4050e.ces.db.theatre.Movie;
import cs4050e.ces.db.payment.Booking;

import java.util.ArrayList;
import java.util.List;

/** Represents a customer as a {@code User} with standard privileges. */
public class Customer extends User {

    /** The max amount of payment cards a customer can have. */
    public static final int MAX_CARDS = 3;

    /** The state of a customer's account.
     * {@code ACTIVE} - The user account is verified through
     * email confirmation
     * {@code INACTIVE} - The user account is created but 
     * not verified.
     * {@code SUSPENDED} - The user account is suspended for
     * disorderly conduct or inactivity.
     */
    public enum CustomerState {
	    ACTIVE,
	    INACTIVE,
	    SUSPENDED
    };

    /** This user's information. */
    private String firstName;
    private String lastName;
    private String mailingAddress;

    /** This user's payment cards. */
    private Card[] paymentCards = new Card[MAX_CARDS];

    /**
     * A list of this user's favorite movies.
     * Stored as a movie's database ID.
     */
    private List<Integer> favorites;

    /** A list of the user's past bookings. */
    private List<Booking> bookingHistory;

    /** State of this customer's account. */
    private CustomerState state;

    public Customer(int id,
		    String email,
		    String password,
		    String firstName,
		    String lastName) {
	
	    super(id, email, password);
    	this.firstName = firstName;
	    this.lastName = lastName;
    	this.state = CustomerState.INACTIVE;
    } // Customer

    public void addFavorite(Movie movie) {
        throw new UnsupportedOperationException("method not yet implemented");
    } // addFavorite

    public void removeFavorite(Movie movie) {
        throw new UnsupportedOperationException("method not yet implemented");
    } // removeFavorite

    public boolean addPaymentCard(Card card) {
        throw new UnsupportedOperationException("method not yet implemented");
    } // addPaymentCard

    public void removePaymentCard(Card card) {
        throw new UnsupportedOperationException("method not yet implemented");
    } // removePaymentCard

    public List<Booking> viewBookingHistory() {
        throw new UnsupportedOperationException("method not yet implemented");
    } // viewBookingHistory
} // Customer
