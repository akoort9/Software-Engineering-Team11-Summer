package cs4050e.deliv2.db;

/** Represents a movie listing. */
public class Listing {
    /** Movie that a listing details. */
    Movie movie;
    
    /** On a 0 to 10 scale, 5 representing 2.5 stars, etc. */
    int rating;
    
    /**
     * {@code true} - "Currently Running"
     * {@code false} - "Coming Soon"
     */
    boolean status;

    /** Hardcoded, here to make it easier to change later. */
    private String[] showtimes = {"2:00 PM", "5:00 PM", "8:00 PM"};

    /**
     * Initializes a {@code Listing} object.
     * @param movie The movie displayed in the listing.
     * @param rating The movie's rating on a 0 to 10 scale.
     * @param status The movie's status.
     * @throws IllegalArgumentException if {@code rating} is not between 0 and 10.
     * @throws NullPointerException if {@code movie} is a null reference.
     */
    public Listing(Movie movie, int rating, boolean status) {
	if (movie == null) {
	    throw new NullPointerException("listing movie cannot be a null reference");
	} // if
	else if (rating < 0 || rating > 10) {
	    throw new IllegalArgumentException("rating must be between 0 and 10");
	} // elif
	else {
	    this.movie = movie;
	    this.status = status;
	    this.rating = rating;
	} //else
    } // Listing

    /**
     * Compares the calling {@code Listing} object to a given {@code Listing}.
     * @param listing the listing to compare to.
     * @return {@code true} if the listings are equal, {@code false} otherwise.
     */
    public boolean compare(Listing listing) {
	if (listing == null) {
	    return false;
	} // if
	
	boolean sameMovie = this.movie.compare(listing.movie);
	boolean sameRating = this.rating == listing.rating;
	boolean sameStatus = this.status == listing.status;

	return sameMovie && sameRating && sameStatus;
    } // compare
    
} // Listing
