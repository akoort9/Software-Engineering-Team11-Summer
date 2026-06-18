package cs4050e.deliv2.db;

/** Represents a movie and its related information. */
public class Movie {
    String title;
    String desc;

    /** Placeholder. Represents URI to movie poster. */
    String poster;
    /** Placeholder. Represents URI to movie trailer. */
    String trailer;

    /**
     * Initializes a {@code Movie} object.
     * @param title The movie's title.
     * @param desc The movie's description.
     * @param poster The movie's promotional poster.
     * @param trailer The movie's promotional trailer.
     */
    public Movie(String title, String desc, String poster, String trailer) {
	this.title = title;
	this.desc = desc;
	this.poster = poster;
	this.trailer = trailer;
    } // Movie
    
} // Movie
