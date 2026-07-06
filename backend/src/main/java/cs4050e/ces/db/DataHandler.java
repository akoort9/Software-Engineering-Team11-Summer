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

/** Provides methods to handle what's stored in a given SQLite database. */
public class DataHandler {
    
    /**
     * Opens a connection to the SQLite database at the given filepath,
     * creating the {@code movies} table if it doesn't already exist.
     * @param filepath The path of the database file.
     * @return an open {@code Connection}.
     * @throws SQLException if the connection or table creation fails.
     */
    private static Connection connect(String filepath) throws SQLException {
		try {
	    	// instantiate the driver directly instead of going through
		    // DriverManager, which can fail to "see" drivers loaded by a
		    // different classloader (happens under mvn exec:java)
	    	java.sql.Driver driver = (java.sql.Driver) Class.forName("org.sqlite.JDBC")
			.getDeclaredConstructor()
			.newInstance();
	    	Connection conn = driver.connect("jdbc:sqlite:" + filepath, new java.util.Properties());

	    	try (Statement stmt = conn.createStatement()) {
				stmt.execute(
					"CREATE TABLE IF NOT EXISTS movies (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"title TEXT NOT NULL, " +
					"genre TEXT, " +
					"\"desc\" TEXT, " +
					"poster TEXT, " +
					"trailer TEXT, " +
					"rating INTEGER, " +
					"status INTEGER, " +
					"showtimes TEXT)"
				);
			} // try

	    	return conn;
		} catch (ReflectiveOperationException roe) {
	    	throw new SQLException("sqlite-jdbc driver not found on classpath", roe);
		} // try-catch
    } // connect

    /**
     * Adds a movie to the provided database.
     * @param movie The {@code Movie} to add.
     * @param filepath The path of the database.
     * @return {@code true} if the operation succeeded, {@code false} otherwise.
     */
    public static boolean addMovie(Movie movie, String filepath) {
		String sql = "INSERT INTO movies (title, genre, \"desc\", poster, trailer, rating, status, showtimes) "
	    	+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = connect(filepath);
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
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
     * @param filepath The filepath of the database.
     * @return a {@code List} of {@code Movie}s if successful, {@code null} otherwise.
     */
    public static List<Movie> getMovies(String filepath) {
		String sql = "SELECT id, title, genre, \"desc\", poster, trailer, rating, status, showtimes FROM movies";
		List<Movie> movies = new ArrayList<Movie>();

		try (Connection conn = connect(filepath);
	    	 Statement stmt = conn.createStatement();
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
				movie.id = rs.getInt("id");
				movies.add(movie);
	    	} // while

		    return movies;
		} catch (SQLException sqle) {
	    	System.err.println("getMovies: " + sqle);
		    return null;
		} // try-catch
    } // getMovies

	public static double getTicketPrice(Ticket.TicketType type) {
		throw new UnsupportedOperationException("method not yet implemented");
	} // getTicketType
} // DataHandler
