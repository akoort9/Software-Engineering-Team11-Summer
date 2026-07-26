package cs4050e.ces.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.io.IOException;

import cs4050e.ces.db.payment.Ticket;
import cs4050e.ces.db.theatre.Movie;
import cs4050e.ces.db.theatre.Seat;
import cs4050e.ces.db.theatre.Showroom;
import cs4050e.ces.db.theatre.Showtime;
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

	/** Singleton object to hash passwords. */
	private static MessageDigest messageDigest;

	/** Singleton constructor. */
	private DataHandler() {
		try {
			// create db file if it doesn't exist
			if (!new File(DB_PATH).isFile()) {
				new File(DB_PATH).createNewFile();
			} // if			
			this.conn = connect(DB_PATH);
		} catch (SQLException sqle) {
			System.err.println("connect: " + sqle);
			return;
		} catch (IOException ioe) {
			System.err.println("connect: " + ioe);	
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
				stmt.execute(Schema.SHOWROOMS_TABLE);
				stmt.execute(Schema.SEATS_TABLE);
				stmt.execute(Schema.SHOWTIMES_TABLE);
				stmt.execute(Schema.TICKETS_TABLE);
				stmt.execute(Schema.PRICES_TABLE);
			} // try

			// keep older database files compatible with newer columns
			ensureColumn(conn, "users", "subscribed_to_promotions", "INTEGER");
			ensureColumn(conn, "users", "verification_code", "TEXT");
			ensureColumn(conn, "users", "reset_code", "TEXT");
			ensureColumn(conn, "users", "reset_code_expires", "TEXT");

	    	return conn;
		} catch (ReflectiveOperationException roe) {
	    	throw new SQLException("sqlite-jdbc driver not found on classpath", roe);
		} // try-catch
    } // connect

    /**
     * Adds a column to a table if it does not already exist, so database
     * files created before a schema change stay usable.
     * @param conn an open database connection.
     * @param table the table to check.
     * @param column the column that should exist.
     * @param type the SQL type of the column.
     */
    private void ensureColumn(Connection conn, String table, String column, String type) {
		// check whether the column already exists
		try (Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
			while (rs.next()) {
				if (column.equals(rs.getString("name"))) {
					return; // already present, nothing to do
				} // if
			} // while
		} catch (SQLException sqle) {
			System.err.println("ensureColumn(check): " + sqle);
			return;
		} // try-catch

		// column is missing, so add it
		try (Statement stmt = conn.createStatement()) {
			stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
		} catch (SQLException sqle) {
			System.err.println("ensureColumn(alter): " + sqle);
		} // try-catch
    } // ensureColumn

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
			movie.setId(getLatestDatabaseId());
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
	 * Returns a movie from the database with the given title.
	 * @param title
	 * @return A {@code Movie} object or {@code null} if it does not exist.
	 */
	public Movie getMovie(int id) {
		String sql = "SELECT * FROM movies WHERE id = " + id;
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
			stmt.setString(4, hashPassword(user.getPassword()));

			if (user.isAdmin()) {
				// user is an admin
				stmt.setString(2, "");
				stmt.setString(5, "admin");
				stmt.setString(6, "");
				stmt.setString(7, "ACTIVE");
				stmt.setInt(8, 0);
			} else {
				// user is a customer
				Customer customer = (Customer) user;
				stmt.setString(2, customer.getLastName());
				stmt.setString(5, "customer");
				stmt.setString(6, customer.getMailingAddress());
				stmt.setString(7, customer.getState().toString());
				stmt.setInt(8, customer.isSubscribedToPromotions() ? 1 : 0);
			} // if-else
	    
		    stmt.executeUpdate();

			// grabbing ID
			user.setId(getLatestDatabaseId());
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
					Customer customer = new Customer(
						rs.getString("first_name"),
						rs.getString("email_address"),
						rs.getString("password_hash"),
						rs.getString("last_name"),
						rs.getString("mailing_address"),
						rs.getString("state")
					);
					customer.setSubscribedToPromotions(rs.getInt("subscribed_to_promotions") == 1);
					user = customer;
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

	/**
	 * Updates a user in the database with a given {@code User}'s 
	 * mutable information. The given {@code User} object must share
	 * the email address of the user you wish to update.
	 * @param user The user 
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean updateUser(User user) {
		String sql;
		User dbUser;

		// error and sanity checking
		try {
			dbUser = getUser(user.getEmail());
			if (dbUser.isAdmin() ^ user.isAdmin()) {
				// cannot update user to a different role
				return false;
			} // if
		} catch (NullPointerException npe) {
			System.err.println("updateUser: " + npe);
			return false;
		} // try-catch

		// update user info according to role
		int userId = resolveUserId(user);
		if (dbUser.isAdmin()) {
			sql = "UPDATE users SET first_name = '" + user.getName() +
			"' WHERE id = " + userId;
		} else {
			Customer customer = (Customer) user;
			sql = "UPDATE users SET first_name = '" + user.getName() +
			"', last_name = '" + customer.getLastName() + 
			"', mailing_address = '" + customer.getMailingAddress() + "' " +
			"WHERE id = " + userId;
		} // if

		// run SQL
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
			return true;
		} catch (SQLException sqle) {
			System.err.println("updateUser: " + sqle);
			return false;
		} // try-catch
	} // updateUser

	/**
	 * Adds a given {@code Movie} to a given {@code User}'s
	 * favorite movie list in the database.
	 * @param user The user who favorited the movie.
	 * @param movie The movie to favorite.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean addFavoriteMovie(User user, Movie movie) {
		int userId = resolveUserId(user);
		int movieId = resolveMovieId(movie);
		if (userId == -1 || movieId == -1) {
			return false;
		} // if

		// run SQL
		String sql = Schema.ADD_FAVORITE_MOVIE;
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setInt(2, movieId);
			stmt.executeUpdate();
			return true;			
		} catch (SQLException sqle) {
			System.err.println("addFavoriteMovie: " + sqle);
			return false;
		} // try-catch
	} // addFavoriteMovie

	/**
	 * Removes a {@code Movie} from a given {@code User}'s
	 * favorite movie list.
	 * @param user The user who favorited the movie.
	 * @param movie The movie to remove.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean removeFavoriteMovie(User user, Movie movie) {
		int userId = resolveUserId(user);
		int movieId = resolveMovieId(movie);
		if (userId == -1 || movieId == -1) {
			return false;
		} // if

		// run SQL
		String sql = Schema.REMOVE_FAVORITE_MOVIE;
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setInt(2, movieId);
			stmt.executeUpdate();
			return true;
		} catch (SQLException sqle) {
			System.err.println("removeFavoriteMovie: " + sqle);
			return false;
		} // try-catch
	} // removeFavoriteMovie

	/**
	 * Grabs a given {@code User}'s favorite movies from the database.
	 * @param user The user
	 * @return A {@code List<Movie>} of all the {@code User}'s
	 * favorite movies.
	 */
	public List<Movie> getFavoriteMovies(User user) {
		int userId = resolveUserId(user);
		if (userId == -1) {
			return null;
		} // if

		List<Movie> movies = new ArrayList<Movie>();

		// run SQL
		String sql = "SELECT movies.* FROM movies " +
			"JOIN favorite_movies ON favorite_movies.movie_id = movies.id " +
			"WHERE favorite_movies.user_id = " + userId;
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
			System.err.println("getFavoriteMovies: " + sqle);
			return null;
		} // try-catch
	} // getFavoriteMovies

	/**
	 * Add a {@code Card} to the database for a given {@code User}.
	 * @param user The user
	 * @param card The card
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean addCard(User user, Card card) {
		int userId = resolveUserId(user);
		if (userId == -1) {
			return false;
		} // if
		
		// encrypt card before entry
		try {
			card = KeyHandler.encryptCard(card);
		} catch (Exception e) {
			System.err.println("encryptCard: " + e);
			return false;
		} // try-catch	

		// run SQL
		String sql = Schema.ADD_CARD;
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setString(2, card.getCardNumber());
			stmt.setString(3, card.getBillingAddress());
			stmt.setString(4, card.getExpirationDate().toString());
			stmt.executeUpdate();
			return true;
		} catch (SQLException sqle) {
			System.err.println("addCard: " + sqle);
			return false;
		} // try-catch
	} // addCard

	/**
	 * Returns a given {@code User}'s stored {@code Card}'s.
	 * @param user The user
	 * @return A {@code List<Card>} of all the {@code User}'s {@code Card}'s.
	 */
	public List<Card> getCards(User user) {
		int userId = resolveUserId(user);
		if (userId == -1) {
			return null;
		} // if

		List<Card> cards = new ArrayList<Card>();

		// run SQL
		String sql = "SELECT * FROM payment_methods WHERE user_id = " + userId;
		try (Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				try {
					String exp = rs.getString("expiration_date");
					int year = 2000;
					int month = 1;
					if (exp != null && exp.contains("-")) {
						String[] parts = exp.split("-");
						year = Integer.parseInt(parts[0]);
						month = Integer.parseInt(parts[1]);
					} // if
					Card card = KeyHandler.decryptCard(new Card(
						rs.getString("card_number"),
						rs.getString("billing_address"),
						year,
						month
					));
					card.setId(rs.getInt("id"));
					cards.add(card);
				} catch (Exception e) {
					// skip a card that can't be decrypted (e.g. stored with an old key)
					System.err.println("getCards (skipping card): " + e);
				} // try-catch
			} // while

			rs.close();
			return cards;
		} catch (Exception e) {
			System.err.println("getCards: " + e);
			return null;
		} // try-catch
	} // getCards

	/**
	 * Removes a {@code Card} belonging to the given {@code User}.
	 * @param user The card's owner.
	 * @param cardId The id of the card to remove.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean removeCard(User user, int cardId) {
		int userId = resolveUserId(user);
		if (userId == -1) {
			return false;
		} // if

		String sql = "DELETE FROM payment_methods WHERE id = ? AND user_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, cardId);
			stmt.setInt(2, userId);
			stmt.executeUpdate();
			return true;
		} catch (SQLException sqle) {
			System.err.println("removeCard: " + sqle);
			return false;
		} // try-catch
	} // removeCard

	/**
	 * Updates a {@code Card} belonging to the given {@code User}.
	 * @param user The card's owner.
	 * @param cardId The id of the card to update.
	 * @param card The new card details.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean updateCard(User user, int cardId, Card card) {
		int userId = resolveUserId(user);
		if (userId == -1) {
			return false;
		} // if

		// encrypt card before entry
		try {
			card = KeyHandler.encryptCard(card);
		} catch (Exception e) {
			System.err.println("encryptCard: " + e);
			return false;
		} // try-catch

		String sql = "UPDATE payment_methods SET card_number = ?, billing_address = ?, "
			+ "expiration_date = ? WHERE id = ? AND user_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, card.getCardNumber());
			stmt.setString(2, card.getBillingAddress());
			stmt.setString(3, card.getExpirationDate().toString());
			stmt.setInt(4, cardId);
			stmt.setInt(5, userId);
			stmt.executeUpdate();
			return true;
		} catch (SQLException sqle) {
			System.err.println("updateCard: " + sqle);
			return false;
		} // try-catch
	} // updateCard

	/**
	 * Resolves a {@code User}'s database id, looking it up by email if unset.
	 * @param user The user.
	 * @return The database id, or {@code -1} if not found.
	 */
	private int resolveUserId(User user) {
		if (user == null) {
			return -1;
		} else if (user.getId() != -1) {
			return user.getId();
		} // if
		User dbUser = getUser(user.getEmail());
		return dbUser == null ? -1 : dbUser.getId();
	} // resolveUserId

	/**
	 * Resolves a {@code Movie}'s database id, looking it up by title if unset.
	 * @param movie The movie.
	 * @return The database id, or {@code -1} if not found.
	 */
	public int resolveMovieId(Movie movie) {
		if (movie == null) {
			return -1;
		} else if (movie.getId() != -1) {
			return movie.getId();
		} // if
		Movie dbMovie = getMovie(movie.getTitle());
		return dbMovie == null ? -1 : dbMovie.getId();
	} // resolveMovieId

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
					if (rs.getString("email_address").equals(email)) {
						return true;
					} // if
				} // while
				return false;
		} catch (SQLException sqle) {
			System.err.println("userExists: " + sqle);
			return false;
		} // try-catch
    } // userExists

	/**
	 * Stores an email-verification code for the user with the given email.
	 * @param email The user's email address.
	 * @param code The verification code to store.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean setVerificationCode(String email, String code) {
		String sql = Schema.SET_VERIFICATION_CODE;
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, code);
			stmt.setString(2, email);
			stmt.executeUpdate();
			return true;
		} catch (SQLException sqle) {
			System.err.println("setVerificationCode: " + sqle);
			return false;
		} // try-catch
	} // setVerificationCode

	/**
	 * Returns the stored verification code for a user, or {@code null}
	 * if none is set (e.g. the account is already verified).
	 * @param email The user's email address.
	 * @return The verification code, or {@code null}.
	 */
	public String getVerificationCode(String email) {
		String sql = Schema.GET_VERIFICATION_CODE;
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("verification_code");
				} // if
			} // try
			return null;
		} catch (SQLException sqle) {
			System.err.println("getVerificationCode: " + sqle);
			return null;
		} // try-catch
	} // getVerificationCode

	/**
	 * Marks a user's account as verified (ACTIVE) and clears their
	 * verification code.
	 * @param email The user's email address.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean activateUser(String email) {
		String sql = Schema.ACTIVATE_USER;
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			stmt.executeUpdate();
			return true;
		} catch (SQLException sqle) {
			System.err.println("activateUser: " + sqle);
			return false;
		} // try-catch
	} // activateUser

	/**
	 * Stores a password-reset code for the user with the given email,
	 * along with when it expires.
	 * @param email The user's email address.
	 * @param code The reset code to store.
	 * @param expiresAt Epoch-millisecond timestamp after which the code is invalid.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean setResetCode(String email, String code, long expiresAt) {
		String sql = Schema.SET_RESET_CODE;
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, code);
			stmt.setString(2, Long.toString(expiresAt));
			stmt.setString(3, email);
			stmt.executeUpdate();
			return true;
		} catch (SQLException sqle) {
			System.err.println("setResetCode: " + sqle);
			return false;
		} // try-catch
	} // setResetCode

	/**
	 * Returns the stored password-reset code for a user, or {@code null}
	 * if none is set.
	 * @param email The user's email address.
	 * @return The reset code, or {@code null}.
	 */
	public String getResetCode(String email) {
		String sql = Schema.GET_RESET_CODE;
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("reset_code");
				} // if
			} // try
			return null;
		} catch (SQLException sqle) {
			System.err.println("getResetCode: " + sqle);
			return null;
		} // try-catch
	} // getResetCode

	/**
	 * Returns when a user's password-reset code expires.
	 * @param email The user's email address.
	 * @return Epoch-millisecond expiry timestamp, or {@code 0} if none is set.
	 */
	public long getResetCodeExpiry(String email) {
		String sql = Schema.GET_RESET_CODE_EXPIRY;
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String raw = rs.getString("reset_code_expires");
					if (raw != null) {
						try {
							return Long.parseLong(raw);
						} catch (NumberFormatException nfe) {
							return 0;
						} // try-catch
					} // if
				} // if
			} // try
			return 0;
		} catch (SQLException sqle) {
			System.err.println("getResetCodeExpiry: " + sqle);
			return 0;
		} // try-catch
	} // getResetCodeExpiry

	/**
	 * Clears a user's password-reset code and expiry.
	 * @param email The user's email address.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean clearResetCode(String email) {
		String sql = Schema.CLEAR_RESET_CODE;
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			stmt.executeUpdate();
			return true;
		} catch (SQLException sqle) {
			System.err.println("clearResetCode: " + sqle);
			return false;
		} // try-catch
	} // clearResetCode

	/**
	 * Sets a new password for a user and clears any pending password-reset
	 * code, so it can't be reused.
	 * @param email The user's email address.
	 * @param newPassword The new password.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean updatePassword(String email, String newPassword) {
		String sql = Schema.UPDATE_PASSWORD;
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, hashPassword(newPassword));
			stmt.setString(2, email);
			stmt.executeUpdate();
			return true;
		} catch (SQLException sqle) {
			System.err.println("updatePassword: " + sqle);
			return false;
		} // try-catch
	} // updatePassword

    /**
     * Hashes the given {@code String} with the SHA-256 algorithm.
     * @param plaintext The string to hash.
     * @return The hashed {@code String}.
     */
    public String hashPassword(String plaintext) {
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException nsae) {
            System.err.println("hashPassword: " + nsae);
            return null;
        } // try-catch
        
        // hash password
		messageDigest.update(plaintext.getBytes());
		return new String(messageDigest.digest());
    } // hashPassword

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
			stmt.execute("DELETE FROM tickets");
			stmt.execute("DELETE FROM showtimes");
			stmt.execute("DELETE FROM seats");
			stmt.execute("DELETE FROM showrooms");
			stmt.execute("DELETE FROM prices");
			stmt.execute("DELETE FROM movies");
			stmt.execute("DELETE FROM users");
			stmt.execute("DELETE FROM favorite_movies");
			stmt.execute("DELETE FROM payment_methods");
		} catch (SQLException sqle) {
			return false;
		} // try-catch

		// reset autoincrement counters so ids restart cleanly; this table
		// only exists once an AUTOINCREMENT table has ever been inserted
		// into, so a fresh, never-used database file won't have it yet
		try (Statement stmt = conn.createStatement()) {
			stmt.execute("DELETE FROM sqlite_sequence");
		} catch (SQLException sqle) {
			// ignore; nothing to reset
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
		for (Movie movie : Seed.movies) {
			this.addMovie(movie); 
		} // for

		// seeds users
		for (User user : Seed.users) {
			this.addUser(user);
		} // for

		// seed fav movie
		addFavoriteMovie(Seed.users[1], Seed.movies[1]);

		// seed cards
		for (int i = 0; i < Seed.cards.length; i++) {
			addCard(Seed.users[2], Seed.cards[i]);
		} // for

		// seed ticket prices
		try (PreparedStatement stmt = conn.prepareStatement(Schema.ADD_PRICES)) {
			stmt.setDouble(1, 12.0);	// standard
			stmt.setDouble(2, 8.0);		// child
			stmt.setDouble(3, 10.0);	// senior
			stmt.executeUpdate();
		} catch (SQLException sqle) {
			System.err.println("seed(prices): " + sqle);
		} // try-catch

		// seed default showrooms and their seats
		String[] showroomNames = { "Theatre 1", "Theatre 2", "Theatre 3" };
		String[] rows = { "A", "B", "C", "D" };
		Showroom showroom = null;

		for (String name : showroomNames) {
			Showroom room = new Showroom(name, 32);
			addShowroom(room);

			for (String row : rows) {
				for (int seatNumber = 1; seatNumber <= 8; seatNumber++) {
					addSeat(new Seat(room.getId(), row, seatNumber));
				} // for
			} // for

			// showtimes below are seeded in the first showroom
			if (showroom == null) {
				showroom = room;
			} // if
		} // for

		// seed showtimes for the currently-running movies
		String[] slots = { "14:00:00", "17:00:00", "20:00:00" };
		java.time.LocalDate today = java.time.LocalDate.now();
		for (Movie movie : Seed.movies) {
			if (!movie.isStatus()) {
				continue;
			} // if

			for (String slot : slots) {
				Timestamp start = Timestamp.valueOf(today + " " + slot);
				Timestamp end = new Timestamp(start.getTime() + (2 * 60 * 60 * 1000));
				addShowtime(new Showtime(movie.getId(), showroom.getId(), start, end));
			} // for
		} // for

		return true;
	} // seed

	/**
	 * Checks if a showroom exists.
	 * @param id The database id of the showroom.
	 * @return {@code true} if it does, {@code false} otherwise.
	 */
	public boolean showroomExists(int id) {
		String sql = "SELECT * FROM showrooms WHERE id = " + id;

		try (Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
				while (rs.next()) {
					if (rs.getInt("id") == id) {
						return true;
					} // if
				} // while
				return false;
		} catch (SQLException sqle) {
			System.err.println("showroomExists: " + sqle);
			return false;
		} // try-catch
	} // showroomExists

	/**
	 * Returns every showroom stored in the database.
	 * @return a {@code List} of {@code Showroom}s if successful, {@code null} otherwise.
	 */
	public List<Showroom> getShowrooms() {
		String sql = "SELECT * FROM showrooms";
		List<Showroom> showrooms = new ArrayList<Showroom>();

		try (Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				Showroom showroom = new Showroom(
					rs.getString("name"),
					rs.getInt("capacity")
				);
				showroom.setId(rs.getInt("id"));
				showrooms.add(showroom);
			} // while

			return showrooms;
		} catch (SQLException sqle) {
			System.err.println("getShowrooms: " + sqle);
			return null;
		} // try-catch
	} // getShowrooms

	/**
	 * Adds a {@code Showroom} to the database.
	 * @param showroom The showroom to add.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean addShowroom(Showroom showroom) {
		String sql = Schema.ADD_SHOWROOM;

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, showroom.getName());
			stmt.setInt(2, showroom.getCapacity());
	    
		    stmt.executeUpdate();

			// get database ID
			showroom.setId(getLatestDatabaseId());
		    return true;
		} catch (SQLException sqle) {
	    	System.err.println("addShowroom: " + sqle);
		    return false;
		} // try-catch
	} // addShowroom

	/**
	 * Adds a {@code Seat} to the database.
	 * @param seat The seat to add.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean addSeat(Seat seat) {
		String sql = Schema.ADD_SEAT;

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, seat.getShowroomId());
			stmt.setString(2, seat.getRowLabel());
			stmt.setInt(3, seat.getSeatNumber());
	    
		    stmt.executeUpdate();

			// get database ID
			seat.setId(getLatestDatabaseId());
		    return true;
		} catch (SQLException sqle) {
	    	System.err.println("addSeat: " + sqle);
		    return false;
		} // try-catch
	} // addSeat

	/**
	 * Returns the database ID of the object just inserted
	 * into the database.
	 * @return the database ID, returns -1 if it fails.
	 * @throws SQLException if the operation fails.
	 */
	private int getLatestDatabaseId() throws SQLException {
		int id = -1;
		Statement get_id_stmt = conn.createStatement();
		ResultSet rs = get_id_stmt.executeQuery("SELECT last_insert_rowid()");
		while (rs.next()) {
			id = rs.getInt("last_insert_rowid()");
		} // while
		rs.close();
		return id;
	} // getLatestDatabaseId

	/**
	 * Sets the price for a given {@code TicketType}.
	 * @param type The type of ticket.
	 * @param price The price of the ticket
	 */
	public void setTicketPrice(Ticket.TicketType type, double price) {
		String sql;

		switch (type) {
			case STANDARD:
				sql = "UPDATE prices SET standard_price = " + price;
				break;
			case CHILD:
				sql = "UPDATE prices SET child_price = " + price;
				break;
			case SENIOR:
				sql = "UPDATE prices SET senior_price = " + price;
				break;
			default:
				return;
		} // switch

		// run SQL
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException sqle) {
			System.err.println("setTicketPrice: " + sqle);
		} // try-catch
	} // setTicketPrice

	/**
	 * Returns the price for a given {@code TicketType}
	 * @param type The type of ticket.
	 * @return The price of that type.
	 */
	public double getTicketPrice(Ticket.TicketType type) {
		String col;
		double price = 0;

		switch (type) {
			case STANDARD:
				col = "standard_price";
				break;
			case CHILD:
				col = "child_price";
				break;
			case SENIOR:
				col = "senior_price";
				break;
			default:
				return price;
		} // switch

		try (Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM prices")) {			
			while (rs.next()) {
				price = rs.getDouble(col);
			} // while

			rs.close();
			return price;
		} catch (SQLException sqle) {
			System.err.println("getTicketPrice: " + sqle);
			return price;
		} // try-catch
	} // getTicketType

	/**
	 * Adds a {@code Showtime} to the database.
	 * @param showtime The showtime to add.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
    public boolean addShowtime(Showtime showtime) {
        String sql = Schema.ADD_SHOWTIME;

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, showtime.getMovieId());
			stmt.setInt(2, showtime.getShowroomId());
			stmt.setTimestamp(3, showtime.getStartTime());
			stmt.setTimestamp(4, showtime.getEndTime());

		    stmt.executeUpdate();

			// get database ID
			showtime.setId(getLatestDatabaseId());
		    return true;
		} catch (SQLException sqle) {
	    	System.err.println("addShowtime: " + sqle);
		    return false;
		} // try-catch
    } // addShowtime

	/**
	 * Checks if a proposed showtime overlaps an existing showtime
	 * in the same showroom.
	 * @param showroomId The database id of the showroom.
	 * @param start The proposed start time.
	 * @param end The proposed end time.
	 * @return {@code true} if there is a conflict, {@code false} otherwise.
	 */
	public boolean hasShowtimeConflict(int showroomId, Timestamp start, Timestamp end) {
		String sql = "SELECT COUNT(*) AS conflicts FROM showtimes " +
					 "WHERE showroom_id = ? AND start_time < ? AND end_time > ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, showroomId);
			stmt.setTimestamp(2, end);
			stmt.setTimestamp(3, start);

			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() && rs.getInt("conflicts") > 0;
			} // try
		} catch (SQLException sqle) {
			System.err.println("hasShowtimeConflict: " + sqle);
			// fail safe: report a conflict so we never double-book on error
			return true;
		} // try-catch
	} // hasShowtimeConflict

	/**
	 * Adds a {@code Ticket} to the database.
	 * @param ticket The ticker to add.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
    public boolean addTicket(Ticket ticket) {
        String sql = Schema.ADD_TICKET;

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, ticket.getUserId());
			stmt.setInt(2, ticket.getShowtimeId());
			stmt.setInt(3, ticket.getSeatId());
			stmt.setDouble(4, ticket.getPrice());
			stmt.setString(5, ticket.getTypeString());
			stmt.setDate(6, ticket.getPurchaseDate());
	    
		    stmt.executeUpdate();

			// get database ID
			ticket.setId(getLatestDatabaseId());
		    return true;
		} catch (SQLException sqle) {
	    	System.err.println("addTicket: " + sqle);
		    return false;
		} // try-catch
    } // addTicket

    /**
     * Returns every showtime stored in the database.
     * @return a {@code List} of {@code Showtime}s if successful, {@code null} otherwise.
     */
    public List<Showtime> getShowtimes() {
        String sql = "SELECT * FROM showtimes";
        List<Showtime> showtimes = new ArrayList<Showtime>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Showtime showtime = new Showtime(
                    rs.getInt("movie_id"),
                    rs.getInt("showroom_id"),
                    rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time")
                );
                showtime.setId(rs.getInt("id"));
                showtimes.add(showtime);
            } // while

            rs.close();
            return showtimes;
        } catch (SQLException sqle) {
            System.err.println("getShowtimes: " + sqle);
            return null;
        } // try-catch
    } // getShowtimes

    /**
     * Returns every showtime stored in the database for a given movie.
     * @param movieId The database id of the movie.
     * @return a {@code List} of {@code Showtime}s if successful, {@code null} otherwise.
     */
    public List<Showtime> getShowtimesForMovie(int movieId) {
        String sql = "SELECT * FROM showtimes WHERE movie_id = " + movieId;
        List<Showtime> showtimes = new ArrayList<Showtime>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Showtime showtime = new Showtime(
                    rs.getInt("movie_id"),
                    rs.getInt("showroom_id"),
                    rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time")
                );
                showtime.setId(rs.getInt("id"));
                showtimes.add(showtime);
            } // while

            rs.close();
            return showtimes;
        } catch (SQLException sqle) {
            System.err.println("getShowtimesForMovie: " + sqle);
            return null;
        } // try-catch
    } // getShowtimesForMovie

    /**
     * Returns a single showtime from the database.
     * @param id The database id of the showtime.
     * @return A {@code Showtime} object, or {@code null} if it does not exist.
     */
    public Showtime getShowtime(int id) {
        String sql = "SELECT * FROM showtimes WHERE id = " + id;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            Showtime showtime = null;
            while (rs.next()) {
                showtime = new Showtime(
                    rs.getInt("movie_id"),
                    rs.getInt("showroom_id"),
                    rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time")
                );
                showtime.setId(rs.getInt("id"));
            } // while

            rs.close();
            return showtime;
        } catch (SQLException sqle) {
            System.err.println("getShowtime: " + sqle);
            return null;
        } // try-catch
    } // getShowtime

    /**
     * Returns every seat belonging to a given showroom.
     * @param showroomId The database id of the showroom.
     * @return a {@code List} of {@code Seat}s if successful, {@code null} otherwise.
     */
    public List<Seat> getSeats(int showroomId) {
        String sql = "SELECT * FROM seats WHERE showroom_id = " + showroomId;
        List<Seat> seats = new ArrayList<Seat>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Seat seat = new Seat(
                    rs.getInt("showroom_id"),
                    rs.getString("row_label"),
                    rs.getInt("seat_number")
                );
                seat.setId(rs.getInt("id"));
                seats.add(seat);
            } // while

            rs.close();
            return seats;
        } catch (SQLException sqle) {
            System.err.println("getSeats: " + sqle);
            return null;
        } // try-catch
    } // getSeats

    /**
     * Returns the database ids of every seat already booked (ticketed)
     * for a given showtime.
     * @param showtimeId The database id of the showtime.
     * @return a {@code List} of seat ids if successful, {@code null} otherwise.
     */
    public List<Integer> getBookedSeatIds(int showtimeId) {
        String sql = "SELECT seat_id FROM tickets WHERE showtime_id = " + showtimeId;
        List<Integer> seatIds = new ArrayList<Integer>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                seatIds.add(rs.getInt("seat_id"));
            } // while

            rs.close();
            return seatIds;
        } catch (SQLException sqle) {
            System.err.println("getBookedSeatIds: " + sqle);
            return null;
        } // try-catch
    } // getBookedSeatIds

    /**
     * Books a list of {@code Ticket}s as a single all-or-nothing operation:
     * if any seat in the list is already booked for its showtime (or any
     * other error occurs), none of the tickets are saved. Real-time seat
     * locking is not implemented; this relies on the {@code tickets} table's
     * unique (showtime_id, seat_id) constraint as the source of truth.
     * @param tickets The tickets to book.
     * @return the booked tickets (with database ids set), or {@code null}
     * if the booking failed.
     */
    public List<Ticket> bookSeats(List<Ticket> tickets) {
        try {
            conn.setAutoCommit(false);

            for (Ticket ticket : tickets) {
                try (PreparedStatement stmt = conn.prepareStatement(Schema.ADD_TICKET)) {
                    stmt.setInt(1, ticket.getUserId());
                    stmt.setInt(2, ticket.getShowtimeId());
                    stmt.setInt(3, ticket.getSeatId());
                    stmt.setDouble(4, ticket.getPrice());
                    stmt.setString(5, ticket.getTypeString());
                    stmt.setDate(6, ticket.getPurchaseDate());
                    stmt.executeUpdate();
                    ticket.setId(getLatestDatabaseId());
                } // try
            } // for

            conn.commit();
            return tickets;
        } catch (SQLException sqle) {
            System.err.println("bookSeats: " + sqle);
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                System.err.println("bookSeats(rollback): " + rollbackException);
            } // try-catch
            return null;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException autoCommitException) {
                System.err.println("bookSeats(autoCommit): " + autoCommitException);
            } // try-catch
        } // try-catch-finally
    } // bookSeats
} // DataHandler
