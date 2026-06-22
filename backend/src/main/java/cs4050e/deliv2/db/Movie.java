package cs4050e.deliv2.db;

/** Represents a movie and its related information. */
public class Movie {
    /** Movie title. */
    String title;
    
    /** Can be made into an {@code enum} later for easier filtering. */
    String genre;

    /** Movie description. */
    String desc;

    /** Placeholder. Represents URI to movie poster. */
    String poster;
    
    /** Placeholder. Represents URI to movie trailer. */
    String trailer;

    /** On a 0 to 10 scale, 5 representing 2.5 stars, etc. */
    int rating;

    /**
     * {@code true} - "Currently Running"
     * {@code false} - "Coming Soon"
     */
    boolean status;

    /** Hardcoded.
     * Comma separated string, each substring is a showtime for
     * the movie. A semicolon denotes the end of the string.
     */
    String showtimes = "2:00 PM,5:00 PM,8:00 PM;";
    
    /**
     * Initializes a {@code Movie} object.
     * @param title The movie's title.
     * @param desc The movie's description.
     * @param poster The movie's promotional poster.
     * @param trailer The movie's promotional trailer.
     * @param rating The movie's rating.
     * @param status Whether the movie is 'Currently Showing' or not ('Coming Soon').
     * @param showtimes A comma separated list of the movie's showtimes. Hardcoded at the moment.
     * @throws IllegalArgumentException if {@code rating} is not between 0 and 10.
     * @throws NullPointerException if any {@code String} parameter is {@code null}.
     */
    public Movie(String title,
		 String genre,
		 String desc,
		 String poster,
		 String trailer,
		 int rating,
		 boolean status) {
	// null check
	if (title == null ||
	     genre == null ||
	     desc == null ||
	     poster == null ||
	     trailer == null) {
	    throw new NullPointerException("no string parameters can be null.");
	} else if (rating < 0 || rating > 10) {
	    throw new IllegalArgumentException("rating must be between 0 and 10");
	} else {
	    this.title = title;
	    this.genre = genre;
	    this.desc = desc;
	    this.poster = poster;
	    this.trailer = trailer;
	    this.status = status;
	    this.rating = rating;
	} // if-else
    } // Movie

    /**
     * Compares the calling {@code Movie} object to a given
     * {@code Movie}.
     * @param movie The movie to compare to.
     * @return {@code true} if the movies are equal, {@code false} otherwise.
     */
    public boolean compare(Movie movie) {
	if (movie == null) {
	    return false;
	} // if
	
	boolean sameTitle = this.title.equals(movie.title);
	boolean sameGenre = this.genre.equals(movie.genre);
	boolean sameDesc = this.desc.equals(movie.desc);
	boolean samePoster = this.poster.equals(movie.poster);
	boolean sameTrailer = this.trailer.equals(movie.trailer);
	boolean sameRating = this.rating == movie.rating;
	boolean sameStatus = this.status == movie.status;
	boolean sameShowtimes = this.showtimes == movie.showtimes;

	return (sameTitle &&
		sameGenre &&
		sameDesc &&
		samePoster &&
		sameTrailer &&
		sameRating &&
		sameStatus &&
		sameShowtimes);
    } // compare
    
} // Movie
