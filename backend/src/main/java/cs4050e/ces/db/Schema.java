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
                    "status INTEGER, " +
                    "rating INTEGER)";

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
                    "user_id INTEGER NOT NULL REFERENCES users(id), " +
                    "movie_id INTEGER NOT NULL REFERENCES movies(id))";

    /** SQL statement to create the 'payment_methods' table. */
    public static final String PAYMENT_METHODS_TABLE = "CREATE TABLE IF NOT EXISTS payment_methods (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL REFERENCES users(id), " +
                    "card_number TEXT, " +
                    "billing_address TEXT, " +
                    "expiration_date DATE)";

    /** SQL statement to create the 'showrooms' table. */
    public static final String SHOWROOMS_TABLE = "CREATE TABLE IF NOT EXISTS showrooms (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "capacity INTEGER)";

    /** SQL statement to create the 'seats' table. */
    public static final String SEATS_TABLE = "CREATE TABLE IF NOT EXISTS seats (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "showroom_id INTEGER NOT NULL REFERENCES showrooms(id), " +
                    "row_label TEXT, " +
                    "seat_number INTEGER)";

    /** SQL statement to create the 'showtimes' table. */
    public static final String SHOWTIMES_TABLE = "CREATE TABLE IF NOT EXISTS showtimes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "movie_id INTEGER NOT NULL REFERENCES movies(id), " +
                    "showroom_id INTEGER NOT NULL REFERENCES showrooms(id), " +
                    "start_time DATETIME, " +
                    "end_time DATETIME)";

    /** SQL statement to create the 'tickets' table. */
    public static final String TICKETS_TABLE = "CREATE TABLE IF NOT EXISTS tickets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL REFERENCES users(id), " +
                    "showtime_id INTEGER NOT NULL REFERENCES showtimes(id), " +
                    "seat_id INTEGER NOT NULL REFERENCES seats(id), " +
                    "price DECIMAL, " +
                    "ticket_class TEXT, " +
                    "purchase_date DATETIME, " +
                    "UNIQUE (showtime_id, seat_id))";

    /** SQL statement to add a movie. */
    public static final String ADD_MOVIE = "INSERT INTO movies (" +
                    "title, genre, \"desc\", poster, trailer, status, rating) " +
    	    	    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    /** SQL statement to add a user. */
    public static final String ADD_USER = "INSERT INTO users (" +
                    "first_name, last_name, email_address, password_hash, role, " +
                    "mailing_address, state) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    /** SQL statement to add a showroom. */
    public static final String ADD_SHOWROOM = "INSERT INTO showrooms (" +
                    "name, capacity) VALUES (?, ?)";

    /** SQL statement to add a seat. */
    public static final String ADD_SEAT = "INSERT INTO seats (" +
                    "showroom_id, row_label, seat_number) VALUES (?, ?, ?)";

    /** SQL statement to add a showtime. */
    public static final String ADD_SHOWTIME = "INSERT INTO showtimes (" +
                    "movie_id, showroom_id, start_time, end_time) VALUES (?, ?, ?, ?)";

    /** SQL statement to add a ticket. */
    public static final String ADD_TICKET = "INSERT INTO tickets (" +
                    "user_id, showtime_id, seat_id, price, ticket_class, purchase_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
    
    /** SQL statement to add a favorite movie. */
    public static final String ADD_FAVORITE_MOVIE = "INSERT INTO favorite_movies (" +
		"user_id, movie_id) VALUES (?, ?)";
} // Schema
