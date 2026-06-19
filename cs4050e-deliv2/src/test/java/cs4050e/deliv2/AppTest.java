package cs4050e.deliv2;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import cs4050e.deliv2.db.Movie;
import cs4050e.deliv2.db.Listing;
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

    Listing listing1 = new Listing(obsession, 9, true);

    Movie chainsawMan = new Movie("Chainsaw Man -- The Movie: Reze Arc",
				  "action",
				  "Chainsaw Man faces his deadliest battle yet in " +
				  "a brutal war between devils, hunters and secret enemies.",
				  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ5EDz7NsypfyMnD8hrsTXfQgufO6SfM_Sh8maLxxmHB1ZnCITA",
				  "https://www.youtube.com/watch?v=tAzAhDNdehs");

    Listing listing2 = new Listing(chainsawMan, 8, true);

    Listing[] listings = {listing1, listing2};
	
    /**
     * Rigorous Test :-)
     */
    @Test
    public void testAddListings() {
        assertTrue(DataHandler.addListings(listings, "./db/listings.db"));
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testGetListings() {
	assertTrue(DataHandler.getListings("./db/listings.db") != null);
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testGetListingsObjects() {
	boolean passing = true;
	Listing[] dbListings = DataHandler.getListings("./db/listings.db");
	for (int i = 0; i < listings.length; i++) {
	    if (passing) {
		passing = dbListings[i].compare(listings[i]);
	    } // if
	    else {
		assertTrue(passing);
	    } // else
	} // for

	assertTrue(passing);
    } 
}
