package cs4050e.ces.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cs4050e.ces.db.payment.Ticket;
import cs4050e.ces.db.theatre.Movie;
import cs4050e.ces.db.users.User;
import cs4050e.ces.db.users.Administrator;
import cs4050e.ces.db.users.Customer;
import cs4050e.ces.db.payment.Card;

/** Singleton class to provide access to the database. */
public class DataHandler {

	/** Singleton object to access the database. */
	private static volatile DataHandler instance = null;

	/** Database filepath. */
	private static final String DB_PATH = "./db/listings.db";

	/** Connection to the database. */
	private Connection conn = null;

	/** Singleton constructor. */
	private DataHandler() {
		try {
			this.conn = connect(DB_PATH);
		} catch (SQLException sqle) {
			System.err.println("connect: " + sqle);
		} // try-catch				
	} // DataHandler

	/**
	 * Returns a reference to the {@code DataHandler}, used
	 * to access the database.
	 * @return The {@code DataHandler} object.
	 */
	public static DataHandler getInstance() {
		DataHandler result = instance;	// read volatile only once
		if (result == null) {	// first check (no locking)
			synchronized(DataHandler.class) {
				result = instance;
				if (result == null) {	// second check (w/locking)
					result = new DataHandler();
					instance = result;
				} // if
			} // sync
		} // if
		return instance;
	} // getInstance

    /**
     * Opens a connection to the SQLite database at the given filepath,
     * creating the {@code movies} table if it doesn't already exist.
     * @return an open {@code Connection}.
     * @throws SQLException if the connection or table creation fails.
     */
    private Connection connect(String filename) throws SQLException {
		try {
	    	// instantiate the driver directly instead of going through
		    // DriverManager, which can fail to "see" drivers loaded by a
		    // different classloader (happens under mvn exec:java)
	    	java.sql.Driver driver = (java.sql.Driver) Class.forName("org.sqlite.JDBC")
			.getDeclaredConstructor()
			.newInstance();
	    	Connection conn = driver.connect("jdbc:sqlite:" + filename, new java.util.Properties());

			// create tables
	    	try (Statement stmt = conn.createStatement()) {
				stmt.execute(Schema.MOVIES_TABLE);
				stmt.execute(Schema.USERS_TABLE);
				stmt.execute(Schema.FAVORITE_MOVIES_TABLE);
				stmt.execute(Schema.PAYMENT_METHODS_TABLE);
			} // try

	    	return conn;
		} catch (ReflectiveOperationException roe) {
	    	throw new SQLException("sqlite-jdbc driver not found on classpath", roe);
		} // try-catch
    } // connect

    /**
     * Adds a movie to the provided database.
     * @param movie The {@code Movie} to add.
     * @return {@code true} if the operation succeeded, {@code false} otherwise.
     */
    public boolean addMovie(Movie movie) {
		String sql = Schema.ADD_MOVIE;

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	    	stmt.setString(1, movie.getTitle());
			stmt.setString(2, movie.getGenre());
		    stmt.setString(3, movie.getDesc());
			stmt.setString(4, movie.getPoster());
		    stmt.setString(5, movie.getTrailer());

	    	if (movie.isStatus()) {
				// Currently Running
				stmt.setInt(6, 1);
		    } else {
				// Coming Soon
				stmt.setInt(6, 0);
		    } // if-else

			stmt.setInt(7, movie.getRating());
	    
		    stmt.executeUpdate();

			// grabbing ID
			Statement get_id_stmt = conn.createStatement();
			ResultSet rs = get_id_stmt.executeQuery("SELECT last_insert_rowid()");
			while (rs.next()) {
				movie.setId(rs.getInt("last_insert_rowid()"));
			} // while

			rs.close();
		    return true;
		} catch (SQLException sqle) {
	    	System.err.println("addMovie: " + sqle);
		    return false;
		} // try-catch
    } // addMovie

    /**
     * Grabs every movie stored in the database.
     * @return a {@code List} of {@code Movie}s if successful, {@code null} otherwise.
     */
    public List<Movie> getMovies() {
		String sql = "SELECT * FROM movies";
		List<Movie> movies = new ArrayList<Movie>();

		try (Statement stmt = conn.createStatement();
	    	 ResultSet rs = stmt.executeQuery(sql)) {
	    	while (rs.next()) {
				Movie movie = new Movie(
		    		rs.getString("title"),
		    		rs.getString("genre"),
		    		rs.getString("desc"),
				    rs.getString("poster"),
				    rs.getString("trailer"),
				    rs.getInt("rating"),
		    		rs.getBoolean("status")
				);

				movie.setId(rs.getInt("id"));
				movies.add(movie);
	    	} // while

			rs.close();
		    return movies;
		} catch (SQLException sqle) {
	    	System.err.println("getMovies: " + sqle);
		    return null;
		} // try-catch
    } // getMovies

	/**
	 * Returns a movie from the database with the given title.
	 * @param title
	 * @return A {@code Movie} object or {@code null} if it does not exist.
	 */
	public Movie getMovie(String title) {
		String sql = "SELECT * FROM movies WHERE title = '" + title + "'";
		try (Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {			
			Movie movie = null;
			while (rs.next()) {
				movie = new Movie(
		    		rs.getString("title"),
		    		rs.getString("genre"),
		    		rs.getString("desc"),
				    rs.getString("poster"),
				    rs.getString("trailer"),
				    rs.getInt("rating"),
		    		rs.getBoolean("status")
				);
				movie.setId(rs.getInt("id"));
			} // while

			rs.close();
			return movie;
		} catch (SQLException sqle) {
			System.err.println("getMovie: " + sqle);
		    return null;
		} // try-catch
	} // getMovie

	/**
	 * Adds a {@code User} to the database.
	 * @param user The user to add.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean addUser(User user) {
		String sql = Schema.ADD_USER;

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	    	stmt.setString(1, user.getName());
			stmt.setString(3, user.getEmail());
		    stmt.setString(4, user.getPassword());

			if (user.isAdmin()) {
				// user is an admin
				stmt.setString(2, "");
				stmt.setString(5, "admin");
				stmt.setString(6, "");
				stmt.setString(7, "ACTIVE");
			} else {
				// user is a customer
				Customer customer = (Customer) user;
				stmt.setString(2, customer.getLastName());
				stmt.setString(5, "customer");
				stmt.setString(6, customer.getMailingAddress());
				stmt.setString(7, customer.getState().toString());
			} // if-else
	    
		    stmt.executeUpdate();

			// grabbing ID
			Statement get_id_stmt = conn.createStatement();
			ResultSet rs = get_id_stmt.executeQuery("SELECT last_insert_rowid()");
			while (rs.next()) {
				user.setId(rs.getInt("last_insert_rowid()"));
			} // while
			
			rs.close();
			return true;
		} catch (SQLException sqle) {
	    	System.err.println("addUser: " + sqle);
		    return false;
		} // try-catch
	} // addUser

	/**
	 * Returns a {@code User} from the database with
	 * the specified email address.
	 * @param emailAddress The email address of the user.
	 * @return A {@code User} object or {@code null} if it does not exist.
	 */
	public User getUser(String emailAddress) {
		String sql = "SELECT * FROM users WHERE email_address = '" + emailAddress + "'";
		User user = null;

		try (Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {			
			while (rs.next()) {
				if (rs.getString("role").equals("admin")) {
					// user is an admin
					user = new Administrator(
						rs.getString("first_name"),
						rs.getString("email_address"),
						rs.getString("password_hash")
					);
				} else {
					// user is a customer
					user = new Customer(
						rs.getString("first_name"),
						rs.getString("email_address"),
						rs.getString("password_hash"),
						rs.getString("last_name"),
						rs.getString("mailing_address"),
						rs.getString("state")
					);
				} // if-else
				user.setId(rs.getInt("id"));
			} // while

			rs.close();
			return user;
		} catch (SQLException sqle) {
			System.err.println("getUser: " + sqle);
		    return null;
		} // try-catch
	} // getUser

	public boolean updateUser(User user) {
		String sql = null;

		// update user info according to role
		if (user.isAdmin()) {
			sql = "UPDATE users SET first_name = '" + user.getName() +
			"' WHERE id = " + user.getId();
		} else {
			Customer customer = (Customer) user;
			sql = "UPDATE users SET first_name = '" + user.getName() +
			"', last_name = '" + customer.getLastName() + 
			"', mailing_address = '" + customer.getMailingAddress() + "' " +
			"WHERE id = " + user.getId();
		} // if

		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
			return true;
		} catch (SQLException sqle) {
			System.err.println("updateUser: " + sqle);
			return false;
		} // try-catch
	} // updateUser

	public boolean addFavoriteMovie(User user, Movie movie) {
		String sql = Schema.ADD_FAVORITE_MOVIE;

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			// check for valid IDs, if not grab objects from DB
			if (user.getId() == -1 || movie.getId() == -1) {
				User dbUser = getUser(user.getEmail());
				Movie dbMovie = getMovie(movie.getTitle());
				stmt.setInt(1, dbUser.getId());
				stmt.setInt(2, dbMovie.getId());
			} else {
				stmt.setInt(1, user.getId());
				stmt.setInt(2, movie.getId());
			} // if-else

			stmt.executeUpdate();
			return true;			
		} catch (SQLException sqle) {
			System.err.println("addFavoriteMovie: " + sqle);
			return false;
		} // try-catch
	} // addFavoriteMovie

	public boolean removeFavoriteMovie(User user, Movie movie) {
		throw new UnsupportedOperationException();
	} // removeFavoriteMovie

	public List<Movie> getFavoriteMovies(User user) {
		throw new UnsupportedOperationException();
	} // getFavoriteMovie

	public boolean addCard(User user, Card card) {
		throw new UnsupportedOperationException();
	} // addCard

	public List<Card> getCards(User user) {
		throw new UnsupportedOperationException();
	} // getCard

	/**
     * Checks if a {@code User} with this email address is in the database.
	 * @param email The user's email address.
     * @return {@code true} if it is, {@code false} otherwise.
     */
    public boolean userExists(String email) {
        String sql = "SELECT * FROM users WHERE email_address = '" + email + "'";

		try (Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
				while (rs.next()) {
					System.out.println("in the while...");
					if (rs.getString("email_address").equals(email)) {
						return true;
					} // if
				} // while
				return false;
		} catch (SQLException sqle) {
			System.err.println("userExists: " + sqle);
			return false;
		} // try-catch
    } // exists

	/**
	 * Wipes the database and reseeds it.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean wipe() {
		if (instance == null) {
			return false;
		} // if

		// clear all records from database, don't drop tables
		try (Statement stmt = conn.createStatement()) {
			stmt.execute("DELETE FROM movies");
			stmt.execute("DELETE FROM users");
			stmt.execute("DELETE FROM favorite_movies");
			stmt.execute("DELETE FROM payment_methods");
		} catch (SQLException sqle) {
			return false;
		} // try-catch

		// reseed
		if (this.seed()) {
			return true;
		} else {
			return false;
		} // if-else
	} // wipe

	/**
	 * Seeds the database.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	private boolean seed() {
		if (instance == null) {
			return false;
		} // if

		// seeds movies
		for (int i = 0; i < Seed.movies.length; i++) {
			this.addMovie(Seed.movies[i]);
		} // for

		// seeds users
		for (int i = 0; i < Seed.users.length; i++) {
			this.addUser(Seed.users[i]);
		} // for

		return true;
	} // seed

	public double getTicketPrice(Ticket.TicketType type) {
		throw new UnsupportedOperationException("method not yet implemented");
	} // getTicketType
} // DataHandler
