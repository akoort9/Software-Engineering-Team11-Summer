package cs4050e.ces.db.users;

/** Represents a user of the system. */
public abstract class User {
    /** The user ID of a given user. */
    protected int id = -1;

    /** The user's first name. */
    protected String name;
    
    /** The user's email address. */
    protected String email;
    
    /** The user's account password. */
    protected String password;

    /** Whether a user is an admin or not. */
    protected boolean isAdmin = false;

    /**
     * Initializes a new {@code User} object.
     * @param name The user's name.
     * @param email The user's email address.
     * @param password The user's account password.
     */
    public User(String name, String email, String password) {
        this.name = name;
	    this.email = email;
	    this.password = password;
    } // User

    /**
     * Returns user ID.
     * @return The database ID.
     */
    public int getId() {
        return id;
    } // getId

    /**
     * Sets the user's database ID.
     * @param id The database ID.
     */
    public void setId(int id) {
        this.id = id;
    } // setId

    /**
     * Returns user's name.
     * @return The name.
     */
    public String getName() {
        return name;
    } // getName

    public void setName(String name) {
        this.name = name;
    } // setName

    /**
     * Returns the user's email address.
     * @return The email address.
     */
    public String getEmail() {
        return email;
    } // getEmail

    /**
     * Returns the user's stored password hash.
     * @return The hashed password.
     */
    public String getPassword() {
        return password;
    } // getPassword

    /**
     * Returns {@code true} if the user is an {@code Administrator}.
     * @return {@code true} if admin, {@code false} otherwise.
     */
    public boolean isAdmin() {
        return this.isAdmin;
    } // isAdmin

    /**
     * Compares two {@code User} objects.
     * @param user The {@code User} to compare the calling {@code User} to.
     * @return {@code true} if equivalent, {@code false} otherwise.
     */
    public boolean compare(User user) {
        boolean sameName = this.name.equals(user.name);
        boolean sameEmail = this.email.equals(user.email);
        boolean samePassword = this.password.equals(user.password);

        return sameName && sameEmail && samePassword;
    } // compare
} // User
