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
  /** Connection to the database. */
	private static final DataHandler db = DataHandler.getInstance();

	Movie movie = new Movie("gleep",
		"gleep",
		"gleep",
		"gleep",
		"gleep",
		5,
		true,
		"2:00 PM,5:00 PM,8:00 PM;");

    /**
     * Seeds the database
     */
    @Test
    public void testSeed() {
      assertTrue(db.wipe());
    } // testSeed

    /**
     * Adds a movie
     */
    @Test
    public void testAddMovie() {
		  assertTrue(db.addMovie(this.movie));
    } // testAddMovie

    
    /**
     * Gets a movie
     */
    @Test
    public void testGetMovie() {
		  Movie dbMovie = db.getMovie(this.movie.getTitle());
      //assertTrue(true);
		  assertNotNull(dbMovie);
    } // testGetMovies

    /**
     * Checks if we got the same movie
     */
    @Test
    public void testGetMovieContents() {
		  Movie dbMovie = db.getMovie(this.movie.getTitle());
		  assertTrue(dbMovie.compare(this.movie));
      //assertTrue(true);
    } // testGetMoviesContents
} // AppTest
