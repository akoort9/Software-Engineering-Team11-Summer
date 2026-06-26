package cs4050e.ces.db.users;

public abstract class User {
    /** The user ID of a given user. */
    protected int id;
    
    /** The user's email address. */
    protected String email;
    
    /** The user's account password. */
    protected String password;

    /**
     * Initializes a new {@code User} object.
     * @param id The user's ID.
     * @param email The user's email address.
     * @param password The user's account password.
     */
    public User(int id, String email, String password) {
	this.id = id;
	this.email = email;
	this.password = password;
    } // User
    
} // User
