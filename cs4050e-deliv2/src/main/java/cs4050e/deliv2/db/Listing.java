package cs4050e.deliv2.db;

/** Represents a movie listing. */
public class Listing {
    Movie movie;
    /** On a 0 to 10 scale, 5 representing 2.5 stars, etc. */
    int rating;

    /** Hardcoded. */
    private String[] showtimes = ["2:00 PM", "5:00 PM", "8:00 PM"];

    /**
     * Initializes a {@code Listing} object.
     * @param movie The movie displayed in the listing.
     * @param rating The movie's rating on a 0 to 10 scale.
     * @throws IllegalArgumentException if rating is not between 0 and 10
     */
    public Listing(Movie movie, int rating) {
	this.movie = movie;
	if (rating < 0 || rating > 10) {
	    throw new IllegalArgumentException("rating must be between 0 and 10");
	} // if
	else {
	    this.rating = rating;
	} // else
    } // Listing
    
} // Listing
