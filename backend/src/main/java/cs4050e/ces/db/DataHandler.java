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

	    	try (Statement stmt = conn.createStatement()) {
				stmt.execute(Schema.MOVIES_TABLE);
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
		    stmt.setInt(6, movie.getRating());

	    	if (movie.isStatus()) {
				// Currently Running
				stmt.setInt(7, 1);
		    } else {
				// Coming Soon
				stmt.setInt(7, 0);
		    } // if-else

	    	stmt.setString(8, movie.getShowtimes());
	    
		    stmt.executeUpdate();
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
		    		rs.getBoolean("status"),
				    rs.getString("showtimes")
				);

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
	 * Wipes the database and reseeds it.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	public boolean wipe() {
		if (instance == null) {
			return false;
		} // if

		// clear all records from database, don't drop tables
		String sql = "DELETE FROM movies";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
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

		Movie[] movies = Seed.movies;

		// seeds movies
		for (int i = 0; i < movies.length; i++) {
			this.addMovie(movies[i]);
		} // for
		return true;
	} // seed

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
		    		rs.getBoolean("status"),
				    rs.getString("showtimes")
				);
			} // while

			return movie;
		} catch (SQLException sqle) {
			System.err.println("addMovie: " + sqle);
		    return null;
		} // try-catch

	} // getMovie

	public double getTicketPrice(Ticket.TicketType type) {
		throw new UnsupportedOperationException("method not yet implemented");
	} // getTicketType
} // DataHandler
