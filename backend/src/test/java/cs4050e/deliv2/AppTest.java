package cs4050e.deliv2;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import cs4050e.deliv2.db.Movie;
import cs4050e.deliv2.db.DataHandler;

/**
 * Unit test for simple App.
 */
public class AppTest {
    Movie obsession = new Movie("Obsession",
				"horror",
				"A movie about a guy who wishes that his coworker " +
				"Nikki loves himself more than anyone else...",
				"http://www.impawards.com/2026/obsession_xlg.html",
				"https://www.youtube.com/watch?v=gMC8kkwbIQQ");

    Movie chainsawMan = new Movie("Chainsaw Man -- The Movie: Reze Arc",
				  "action",
				  "Chainsaw Man faces his deadliest battle yet in " +
				  "a brutal war between devils, hunters and secret enemies.",
				  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ5EDz7NsypfyMnD8hrsTXfQgufO6SfM_Sh8maLxxmHB1ZnCITA",
				  "https://www.youtube.com/watch?v=tAzAhDNdehs");

    Movie[] movies = {obsession, chainsawMan};

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testAddMovie() {
	for (Movie movie : movies) {
	    assertTrue(DataHandler.addMovie(movie, "./db/listings.db"));
	} // for
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testGetMovies() {
	assertNotNull(DataHandler.getMovies("./db/listings.db"));
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testGetMoviesContents() {
	for (Movie movie : movies) {
	    DataHandler.addMovie(movie, "./db/listings.db");
	} // for

	List<Movie> dbMovies = DataHandler.getMovies("./db/listings.db");
	boolean foundObsession = false;

	for (Movie movie : dbMovies) {
	    if (movie.compare(obsession)) {
		foundObsession = true;
	    } // if
	} // for

	assertTrue(foundObsession);
    }
}
