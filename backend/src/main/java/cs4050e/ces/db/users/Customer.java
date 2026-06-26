package cs4050e.ces.db.users;

import cs4050e.ces.db.payment.Card;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {

    /** The max amount of payment cards a customer can have. */
    public static final int MAX_CARDS = 3;

    /** The state of a customer's account. */
    public enum State {
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

    /** State of this customer's account. */
    private State customerState;

    public Customer(int id,
		    String email,
		    String password,
		    String firstName,
		    String lastName) {
	
	super(id, email, password);
	this.firstName = firstName;
	this.lastName = lastName;
	this.customerState = State.INACTIVE;
    } // Customer
} // Customer
