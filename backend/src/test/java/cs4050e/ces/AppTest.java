package cs4050e.ces;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import cs4050e.ces.db.DataHandler;
import cs4050e.ces.db.theatre.Movie;
import cs4050e.ces.db.users.*;;

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

  Customer customer = new Customer("john",
    "johndaboss@epic.com",
    "password",
    "boss",
    "4530 Sequoia Dr, Oakwood GA 30566",
    "ACTIVE"
  );

  Administrator admin = new Administrator("admin",
    "admin@ces.com",
    "admin_password"
  );

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
     * Adds a customer
     */
    @Test
    public void testAddCustomer() {
		  assertTrue(db.addUser(this.customer));
    } // testAddCustomer

    /**
     * Adds an admin
     */
    @Test
    public void testAddAdmin() {
		  assertTrue(db.addUser(this.admin));
    } // testAddAdmin

    
    /**
     * Gets a movie
     */
    @Test
    public void testGetMovie() {
		  Movie dbMovie = db.getMovie(this.movie.getTitle());
		  assertNotNull(dbMovie);
    } // testGetMovie

    /**
     * Gets a customer
     */
    @Test
    public void testGetCustomer() {
		  Customer customer = (Customer) db.getUser(this.customer.getEmail());
		  assertNotNull(customer);
    } // testGetCustomer

    /**
     * Gets an admin
     */
    @Test
    public void testGetAdmin() {
		  Administrator admin = (Administrator) db.getUser(this.admin.getEmail());
		  assertNotNull(admin);
    } // testGetAdmin

    /**
     * Checks if we got the same movie
     */
    @Test
    public void testGetMovieContents() {
		  Movie dbMovie = db.getMovie(this.movie.getTitle());
		  assertTrue(dbMovie.compare(this.movie));
    } // testGetMovieContents

    /**
     * Checks if we got the same customer
     */
    @Test
    public void testGetCustomerContents() {
		  Customer customer = (Customer) db.getUser(this.customer.getEmail());
		  assertTrue(customer.compare(this.customer));
    } // testGetCustomerContents

    /**
     * Checks if we got the same admin
     */
    @Test
    public void testGetAdminContents() {
		  Administrator admin = (Administrator) db.getUser(this.admin.getEmail());
		  assertTrue(admin.compare(this.admin));
    } // testGetCustomerContents

    /**
     * Updates a customer's info
     */
    @Test
    public void testUpdateCustomer() {
      Customer fred = new Customer("fred",
    "johndaboss@epic.com",
    "password",
    "boss",
    "4530 Sequoia Dr, Oakwood GA 30566",
    "ACTIVE"
      );
		  assertTrue(db.updateUser(fred));
      //assertTrue(db.getUser(customer.getEmail()).compare(fred));
		} // testUpdateCustomer

    /**
     * Updates an admin's name
     */
    @Test
    public void testUpdateAdmin() {
      Administrator epicAdmin = new Administrator("epic_admin",
    "admin@ces.com",
    "admin_password"
      );

		  assertTrue(db.updateUser(epicAdmin));
    } // testUpdateAdmin

    /**
     * Check if info got updated
     */
    @Test
    public void testCheckUpdateCustomer() {
      assertTrue(db.getUser(customer.getEmail()).getName().equals("fred"));
		} // testCheckUpdateCustomer

    /**
     * Check if info got updated
     */
    @Test
    public void testCheckUpdateAdmin() {
      assertTrue(db.getUser(admin.getEmail()).getName().equals("epic_admin"));
		} // testCheckUpdateAdmin

} // AppTest
