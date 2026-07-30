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
                    "state TEXT, " +
                    "subscribed_to_promotions INTEGER, " +
                    "verification_code TEXT, " +
                    "reset_code TEXT, " +
                    "reset_code_expires TEXT)";

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

    /** SQL statement to create the 'bookings' table. */
    public static final String BOOKINGS_TABLE = "CREATE TABLE IF NOT EXISTS bookings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL REFERENCES users(id), " +
                    "booking_date DATETIME, " +
                    "total_price DECIMAL, " +
                    "state TEXT)";                

    /** SQL statement to create the 'tickets' table. */
    public static final String TICKETS_TABLE = "CREATE TABLE IF NOT EXISTS tickets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL REFERENCES users(id), " +
                "booking_id INTEGER NOT NULL REFERENCES bookings(id), " +
                "showtime_id INTEGER NOT NULL REFERENCES showtimes(id), " +
                "seat_id INTEGER NOT NULL REFERENCES seats(id), " +
                "price DECIMAL, " +
                "ticket_class TEXT, " +
                "purchase_date DATETIME, " +
                "UNIQUE (showtime_id, seat_id))";
    
    /** SQL statement to create the 'ticket price' table. */
    public static final String PRICES_TABLE = "CREATE TABLE IF NOT EXISTS prices (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "standard_price INTEGER NOT NULL, " +
                    "child_price INTEGER NOT NULL, " +
                    "senior_price INTEGER NOT NULL)";

    /** SQL statement to create the 'promotions' table. */
    public static final String PROMOTIONS_TABLE = "CREATE TABLE IF NOT EXISTS promotions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "promo_code TEXT NOT NULL, " +
                    "discount_percent DECIMAL, " +
                    "expiration_date DATE)";

    /** SQL statement to create the 'payments' table. */
    public static final String PAYMENTS_TABLE = "CREATE TABLE IF NOT EXISTS payments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL REFERENCES users(id), " +
                    "amount DECIMAL, " +
                    "card_last4 TEXT, " +
                    "status TEXT, " +
                    "transaction_id TEXT, " +
                    "created_at DATETIME)";

    /** SQL statement to add a movie. */
    public static final String ADD_MOVIE = "INSERT INTO movies (" +
                    "title, genre, \"desc\", poster, trailer, status, rating) " +
    	    	        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    /** SQL statement to add a user. */
    public static final String ADD_USER = "INSERT INTO users (" +
                    "first_name, last_name, email_address, password_hash, role, " +
                    "mailing_address, state, subscribed_to_promotions) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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
/** SQL statement to add a ticket. */
    public static final String ADD_TICKET = "INSERT INTO tickets (" +
                    "user_id, booking_id, showtime_id, seat_id, price, ticket_class, purchase_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    /** SQL statement to add a row of ticket prices. */
    public static final String ADD_PRICES = "INSERT INTO prices (" +
                    "standard_price, child_price, senior_price) VALUES (?, ?, ?)";

    /** SQL statement to add a promotion. */
    public static final String ADD_PROMOTION = "INSERT INTO promotions (" +
                    "promo_code, discount_percent, expiration_date) VALUES (?, ?, ?)";

    /** SQL statement to add a payment. */
    public static final String ADD_PAYMENT = "INSERT INTO payments (" +
                    "user_id, amount, card_last4, status, transaction_id, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    /** SQL statement to set a user's account state. */
    public static final String SET_USER_STATE = "UPDATE users SET state = ? " +
                    "WHERE email_address = ?";

    /** SQL statement to add a favorite movie. */
    public static final String ADD_FAVORITE_MOVIE = "INSERT INTO favorite_movies (" +
		                "user_id, movie_id) VALUES (?, ?)";

    /** SQL statement to remove a favorite movie. */
    public static final String REMOVE_FAVORITE_MOVIE = "DELETE FROM favorite_movies " +
                    "WHERE user_id = ? AND movie_id = ?";

    /** SQL statement to add a card. */
    public static final String ADD_CARD = "INSERT INTO payment_methods (user_id, card_number, " +
		                "billing_address, expiration_date) VALUES (?, ?, ?, ?)";

    /** SQL statement to set a verification code. */
    public static final String SET_VERIFICATION_CODE = "UPDATE users SET verification_code = ? " +
                    "WHERE email_address = ?";
    
    /** SQL statement to get a verification code. */
    public static final String GET_VERIFICATION_CODE = "SELECT verification_code FROM users " +
                    "WHERE email_address = ?";
    
    /** SQL statement to activate a user. */
    public static final String ACTIVATE_USER = "UPDATE users SET state = 'ACTIVE', " +
                    "verification_code = NULL WHERE email_address = ?";
    
    /** SQL statement to set a password reset code. */
    public static final String SET_RESET_CODE = "UPDATE users SET reset_code = ?, " +
                    "reset_code_expires = ? WHERE email_address = ?";
    
    /** SQL statement to get a password reset code. */
    public static final String GET_RESET_CODE = "SELECT reset_code FROM users WHERE email_address = ?";

    /** SQL statement to get a password reset code expiry date. */
    public static final String GET_RESET_CODE_EXPIRY = "SELECT reset_code_expires FROM users " +
                    "WHERE email_address = ?";
    
    /** SQL statement to clear a password reset code. */
    public static final String CLEAR_RESET_CODE = "UPDATE users SET reset_code = NULL, " +
                    "reset_code_expires = NULL WHERE email_address = ?";

    /** SQL statement to update a user's password. */
    public static final String UPDATE_PASSWORD = "UPDATE users SET password_hash = ?, " +
                    "reset_code = NULL, reset_code_expires = NULL WHERE email_address = ?";

   /** SQL statement to add a booking. */
    public static final String ADD_BOOKING = "INSERT INTO bookings (" +
                    "user_id, booking_date, total_price, state) VALUES (?, ?, ?, ?)";


    /** */


    /** */
} // Schema
