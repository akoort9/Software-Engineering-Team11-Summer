package cs4050e.ces.db;

/** Holds long SQL statements for creating and modifying the database. */
class Schema {

    /** SQL statement to create the 'movies' table. */
    public static final String MOVIES_TABLE = "CREATE TABLE IF NOT EXISTS movies (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"title TEXT NOT NULL, " +
					"genre TEXT, " +
					"\"desc\" TEXT, " +
					"poster TEXT, " +
					"trailer TEXT, " +
					"rating INTEGER, " +
					"status INTEGER, " +
					"showtimes TEXT)";

    /** SQL statement to create the 'users' table. */
    public static final String USERS_TABLE = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "first_name TEXT, " +
                    "last_name TEXT, " +
                    "email_address TEXT NOT NULL, " +
                    "password_hash TEXT, " +
                    "role TEXT, " +
                    "mailing_address TEXT, " +
                    "state TEXT)";

    /** SQL statement to create the 'favorite_movies' table. */
    public static final String FAVORITE_MOVIES_TABLE = "CREATE TABLE IF NOT EXISTS favorite_movies (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "favorite_movie INTEGER NOT NULL)";

    /** SQL statement to create the 'payment_cards' table. */
    public static final String PAYMENT_METHODS_TABLE = "CREATE TABLE IF NOT EXISTS payment_methods (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "card_number TEXT, " +
                    "billing_address TEXT, " +
                    "expiration_date Date)";

    /** SQL statement to add a movie. */
    public static final String ADD_MOVIE = "INSERT INTO movies (" +
                    "title, genre, \"desc\", poster, trailer, rating, status, showtimes) " +
	    	        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    public static final String ADD_USER = "INSERT INTO users (" +
                    "first_name, last_name, email_address, password_hash, role, " +
                    "mailing_address, state) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
} // Schema