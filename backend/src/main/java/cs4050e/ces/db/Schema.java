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
    public static final String USERS_TABLE = "";

    /** SQL statement to create the 'customers' table. */
    public static final String CUSTOMERS_TABLE = "";
    
    /** SQL statement to create the 'admins' table. */
    public static final String ADMINS_TABLE = "";

    /** SQL statement to create the 'passwords' table. */
    public static final String PASSWORDS_TABLE = "";

    /** SQL statement to create the 'favorite_movies' table. */
    public static final String FAVORITE_MOVIES_TABLE = "";

    /** SQL statement to create the 'user_cards' table. */
    public static final String USER_CARDS_TABLE = "";

    /** SQL statement to create the 'payment_cards' table. */
    public static final String PAYMENT_CARDS_TABLE = "";

    /** SQL statement to add a movie. */
    public static final String ADD_MOVIE = "INSERT INTO movies (" +
                    "title, genre, \"desc\", poster, trailer, rating, status, showtimes) " +
	    	        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
} // Schema