package cs4050e.ces.db.users;

/** Represents a {@code User} with administrative privileges. */
public class Administrator extends User {

    /**
     * Initializes a new {@code Administrator} object.
     * @param name The user's name.
     * @param The user's email.
     * @param The user's password.
     */
    public Administrator(String name, String email, String password) {
	    super(name, email, password);
        this.isAdmin = true;
    } // Administrator
} // Administrator
