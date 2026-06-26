package cs4050e.ces.db.users;

public class Administrator extends User {
    /** The name of this administrator. */
    private String name;

    /**
     * Initializes a new {@code Administrator} object.
     * @param The user ID.
     * @param The user's email.
     * @param The user's password.
     * @param name The user's name.
     */
    public Administrator(int id, String email, String password, String name) {
	super(id, email, password);
	this.name = name;
    } // Administrator
} // Administrator
