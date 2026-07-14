package cs4050e.ces.db.users;

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
    private String lastName = "";
    private String mailingAddress = "";

    /** State of this customer's account. */
    private CustomerState state = CustomerState.INACTIVE;

    /**
     * Initializes a new {@code Customer} object.
     * @param firstName The customer's first name.
     * @param email The customer's email address.
     * @param password The customer's hashed password.
     * @param lastName The customer's last name.
     * @param mailingAddress The customer's mailing address.
     * @param state The state of the customer's account.
     */
    public Customer(String firstName,
                    String email,           
                    String password,       
                    String lastName, 
                    String mailingAddress,
                    String state) {
	
	    super(firstName, email, password);
	    this.lastName = lastName;
        this.mailingAddress = mailingAddress;
    	setState(state);
    } // Customer

    /**
     * Returns the user's last name.
     * @return The last name.
     */
    public String getLastName() {
        return lastName;
    } // getLastName

    /**
     * Return the user's mailing address.
     * @return The mailing address.
     */
    public String getMailingAddress() {
        return mailingAddress;
    } // getMailingAddress

    /**
     * Returns the state of the customer's account.
     * @return The user state.
     */
    public CustomerState getState() {
        return state;
    } // getState

    public void setState(String state) {
        if (state.equals("ACTIVE")) {
            this.state = CustomerState.ACTIVE;
        } else if (state.equals("INACTIVE")) {
            this.state = CustomerState.INACTIVE;
        } else if (state.equals("SUSPENDED")) {
            this.state = CustomerState.SUSPENDED;
        } else {
            this.state = CustomerState.INACTIVE;
        } // if-else
    } // setState
} // Customer
