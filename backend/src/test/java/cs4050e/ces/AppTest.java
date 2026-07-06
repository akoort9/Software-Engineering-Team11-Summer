package cs4050e.ces;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import cs4050e.ces.db.DataHandler;
import cs4050e.ces.db.theatre.Movie;

/**
 * Unit test for simple App.
 */
public class AppTest {
    
	private static final String DB_PATH = "./db/listings.db";

	List<Movie> seedMovies = DataHandler.getMovies(DB_PATH);

	Movie movie = new Movie("title",
		"genre",
		"description",
		"poster",
		"trailer",
		5,
		true,
		"2:00 PM,5:00 PM,8:00 PM;");

    /**
     * Adds a movie
     */
    @Test
    public void testAddMovie() {
		assertTrue(DataHandler.addMovie(movie, DB_PATH));
    }

    /**
     * Gets a movie
     */
    @Test
    public void testGetMovies() {
		seedMovies = DataHandler.getMovies(DB_PATH);
		assertNotNull(seedMovies.get(0));
    }

    /**
     * Checks if we got the same movie
     */
    @Test
    public void testGetMoviesContents() {
		seedMovies = DataHandler.getMovies(DB_PATH);
		Movie dbMovie = seedMovies.get(0);
		assertTrue(movie.compare(dbMovie));
    }
}
