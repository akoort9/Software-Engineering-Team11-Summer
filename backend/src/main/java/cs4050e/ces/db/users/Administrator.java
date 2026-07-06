package cs4050e.ces.db.users;

import cs4050e.ces.db.theatre.Movie;
import cs4050e.ces.db.theatre.Showtime;

/** Represents a {@code User} with administrative privileges. */
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

    public void addMovie(Movie movie) {
        throw new UnsupportedOperationException("method not yet implemented");
    } // addMovie

    public void updateMovie(Movie movie) {
        throw new UnsupportedOperationException("method not yet implemented");
    } // addMovie

    public void removeMovie(Movie movie) {
        throw new UnsupportedOperationException("method not yet implemented");
    } // addMovie

    public void scheduleShowtime(Showtime showtime) {
        throw new UnsupportedOperationException("method not yet implemented");
    } // addMovie
} // Administrator
