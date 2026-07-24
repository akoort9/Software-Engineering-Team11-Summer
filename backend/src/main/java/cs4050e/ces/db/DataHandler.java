package cs4050e.ces.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.io.IOException;

import cs4050e.ces.db.payment.Ticket;
import cs4050e.ces.db.theatre.Movie;
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

	public double getTicketPrice(Ticket.TicketType type) {
		throw new UnsupportedOperationException("method not yet implemented");
	} // getTicketType

    public boolean addShowtime(Showtime showtime) {
        throw new UnsupportedOperationException("Unimplemented method 'addShowtime'");
    } // addShowtime

    public List<Showtime> getShowtimes() {
        throw new UnsupportedOperationException("Unimplemented method 'getShowtimes'");
    } // getShowtimes
} // DataHandler
