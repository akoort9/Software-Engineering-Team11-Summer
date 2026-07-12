package cs4050e.ces;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import cs4050e.ces.db.DataHandler;
import cs4050e.ces.db.theatre.Movie;
import cs4050e.ces.db.users.*;
import cs4050e.ces.db.users.Customer.CustomerState;;

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
      db.wipe();
    } // testAddMovie

    /**
     * Adds a customer
     */
    @Test
    public void testAddCustomer() {
		  assertTrue(db.addUser(this.customer));
      db.wipe();
    } // testAddCustomer

    /**
     * Adds an admin
     */
    @Test
    public void testAddAdmin() {
		  assertTrue(db.addUser(this.admin));
      db.wipe();
    } // testAddAdmin

    
    /**
     * Gets a movie
     */
    @Test
    public void testGetMovie() {
      db.addMovie(this.movie);
		  Movie dbMovie = db.getMovie(this.movie.getTitle());
		  assertNotNull(dbMovie);
      db.wipe();
    } // testGetMovie

    /**
     * Gets a customer
     */
    @Test
    public void testGetCustomer() {
      db.addUser(this.customer);
		  Customer customer = (Customer) db.getUser(this.customer.getEmail());
		  assertNotNull(customer);
      db.wipe();
    } // testGetCustomer

    /**
     * Gets an admin
     */
    @Test
    public void testGetAdmin() {
      db.addUser(this.admin);
		  Administrator admin = (Administrator) db.getUser(this.admin.getEmail());
		  assertNotNull(admin);
      db.wipe();
    } // testGetAdmin

    /**
     * Checks if we got the same movie
     */
    @Test
    public void testGetMovieContents() {
		  db.addMovie(this.movie);
		  Movie dbMovie = db.getMovie(this.movie.getTitle());
		  assertTrue(dbMovie.compare(this.movie));
      db.wipe();
    } // testGetMovieContents

    /**
     * Checks if we got the same customer
     */
    @Test
    public void testGetCustomerContents() {
      db.addUser(this.customer);
		  Customer customer = (Customer) db.getUser(this.customer.getEmail());
		  assertTrue(customer.compare(this.customer));
      db.wipe();
    } // testGetCustomerContents

    /**
     * Checks if we got the same admin
     */
    @Test
    public void testGetAdminContents() {
      db.addUser(this.admin);
		  Administrator admin = (Administrator) db.getUser(this.admin.getEmail());
		  assertTrue(admin.compare(this.admin));
      db.wipe();
    } // testGetCustomerContents

    /**
     * Updates a customer's info
     */
    @Test
    public void testUpdateCustomer() {
      db.addUser(this.customer);
      Customer epic_john = (Customer) db.getUser(this.customer.getEmail());
      epic_john.setName("epic_john");
      assertTrue(db.updateUser(epic_john));
      db.wipe();
		} // testUpdateCustomer

    /**
     * Updates an admin's name
     */
    @Test
    public void testUpdateAdmin() {
      db.addUser(this.admin);
      Administrator epic_admin = (Administrator) db.getUser(this.admin.getEmail());
      epic_admin.setName("epic_admin");
      assertTrue(db.updateUser(epic_admin));
      db.wipe();
    } // testUpdateAdmin

    /**
     * Check if info got updated
     */
    @Test
    public void testCheckUpdateCustomer() {
      db.addUser(this.customer);
      Customer epic_john = (Customer) db.getUser(this.customer.getEmail());
      epic_john.setName("epic_john");
      db.updateUser(epic_john);
      User temp = db.getUser(this.customer.getEmail());
      assertTrue(temp.compare(epic_john));
      db.wipe();
		} // testCheckUpdateCustomer

    /**
     * Check if info got updated
     */
    @Test
    public void testCheckUpdateAdmin() {
      db.addUser(this.admin);
      Administrator epic_admin = (Administrator) db.getUser(this.admin.getEmail());
      epic_admin.setName("epic_admin");
      db.updateUser(epic_admin);
      User temp = db.getUser(this.admin.getEmail());
      assertTrue(temp.compare(epic_admin));
      db.wipe();
		} // testCheckUpdateAdmin

    /**
     * Adds a user's favorite movie
     */
    @Test
    public void TestAddFavoriteMovie() {
      db.addUser(this.customer);
      db.addMovie(this.movie);
      assertTrue(db.addFavoriteMovie(this.customer, this.movie));
      db.wipe();
    } // testAddFavoriteMovie

} // AppTest
