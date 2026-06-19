package cs4050e.deliv2.db;

/** Represents a movie and its related information. */
public class Movie {
    String title;
    String genre;
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
    public Movie(String title, String genre, String desc, String poster, String trailer) {
	this.title = title;
	this.genre = genre;
	this.desc = desc;
	this.poster = poster;
	this.trailer = trailer;
    } // Movie

    /**
     * Compares the calling {@code Movie} object to a given
     * {@code Movie}.
     * @param movie the movie to compare to
     * @return {@code true} if the movies are equal, {@code false} otherwise.
     */
    public boolean compare(Movie movie) {
	boolean sameTitle = this.title.equals(movie.title);
	boolean sameGenre = this.genre.equals(movie.genre);
	boolean sameDesc = this.desc.equals(movie.desc);
	boolean samePoster = this.poster.equals(movie.poster);
	boolean sameTrailer = this.trailer.equals(movie.trailer);

	return sameTitle && sameGenre && sameDesc && samePoster && sameTrailer;
    } // compare
    
} // Movie
