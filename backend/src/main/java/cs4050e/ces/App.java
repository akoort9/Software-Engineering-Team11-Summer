package cs4050e.ces;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import java.io.IOException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs4050e.ces.db.DataHandler;
import cs4050e.ces.db.theatre.Movie;
import cs4050e.ces.db.payment.Card;
import cs4050e.ces.db.users.*;

/** Runs the HTTP API that the React frontend talks to. */
public class App {
    /** Port the server listens on. */
    private static final int PORT = 8080;

    /** Connection to the database. */
    private static final DataHandler db = DataHandler.getInstance();

    /** GSON object. */
    private static final Gson GSON = new GsonBuilder().create();

    /** Source of randomness for verification codes. */
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

    /**
     * Starts the HTTP server.
     * @param args unused.
     * @throws IOException if the server fails to start.
     */
    public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

		server.createContext("/api/movies", App::handleMovies);
		server.createContext("/api/user", App::handleUsers);
		server.createContext("/api/favorites", App::handleFavorites);
		server.createContext("/api/cards", App::handleCards);
		server.createContext("/api/login", App::handleLogin);
		server.createContext("/api/verify", App::handleVerify);
		server.setExecutor(null);
		server.start();
		System.out.println("Listening on http://localhost:" + PORT);
    } // main

    /**
     * Routes requests to {@code /api/movies} based on HTTP method.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleMovies(HttpExchange exchange) throws IOException {
		// allow the React dev server (different origin) to call this API
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
		} // if
		else if (method.equals("GET")) {
			handleGetMovies(exchange);
		} // elif
		else if (method.equals("POST")) {
			handlePostMovie(exchange);
		} // elif
		else {
			sendJson(exchange, 405, Map.of("error", "method not allowed"));
		} // else
    } // handleMovies

    /**
     * Returns every movie stored in the database as JSON.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleGetMovies(HttpExchange exchange) throws IOException {
		List<Movie> movies = db.getMovies();

		if (movies == null) {
			sendJson(exchange, 500, Map.of("error", "could not read database"));
			return;
		} // if

		sendJson(exchange, 200, movies);
    } // handleGetMovies

    /**
     * Reads a movie title from the request body, builds a {@code Movie}
     * (other fields default to empty), and stores it in the database.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handlePostMovie(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		Map<?, ?> json;

		try {
			json = GSON.fromJson(body, Map.class);
		} catch (Exception e) {
			sendJson(exchange, 400, Map.of("error", "invalid JSON"));
			return;
		} // try-catch

		Object titleObj = (json == null ? null : json.get("title"));
		String title = titleObj == null ? "" : titleObj.toString().trim();

		if (title.isEmpty()) {
			sendJson(exchange, 400, Map.of("error", "title is required"));
			return;
		} // if

		// only the title comes from the user right now; everything else defaults to empty
		Movie movie = new Movie(title, "", "", "", "", 0, false);
		boolean saved = db.addMovie(movie);

		if (!saved) {
			sendJson(exchange, 500, Map.of("error", "could not save movie"));
			return;
		} // if

		sendJson(exchange, 201, movie);
    } // handlePostMovie

	/**
     * Routes requests to {@code /api/users} based on HTTP method.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleUsers(HttpExchange exchange) throws IOException {
		// allow the React dev server (different origin) to call this API
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
		} // if
		else if (method.equals("GET")) {
			handleGetUser(exchange);
		} // elif
		else if (method.equals("POST")) {
			handlePostUser(exchange);
		} // elif
		else if (method.equals("PUT")) {
			handleUpdateUser(exchange);
		} // elif
		else {
			sendJson(exchange, 405, Map.of("error", "method not allowed"));
		} // else
    } // handleUsers

	/**
     * Returns a given user stored in the database as JSON.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleGetUser(HttpExchange exchange) throws IOException {
		//System.out.println("woah!");

		String queryString = exchange.getRequestURI().getQuery();

		// if there's no '?email=' in the query
		if (!queryString.split("=", 2)[0].equals("email")) {
			sendJson(exchange, 500, Map.of("error", "invalid query"));
			return;
		} // if

		String email = queryString.split("=", 2)[1];
		//System.out.println("we got an email! : " + email);

		// user with email does not exist
		if (!db.userExists(email)) {
			sendJson(exchange, 500, Map.of("error", "could not find user"));
			return;
		} // if

		sendJson(exchange, 200, db.getUser(email));
    } // handleGetMovies

	/**
     * Reads a user from the request body, builds a {@code User},
     * and stores it in the database.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handlePostUser(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		Map<?, ?> json;
		User user;

		try {
			json = GSON.fromJson(body, Map.class);
		} catch (Exception e) {
			sendJson(exchange, 400, Map.of("error", "invalid JSON"));
			return;
		} // try-catch

		// checking if user is an admin
		Object isAdminObj = (json == null ? null : json.get("isAdmin"));
		boolean isAdmin = (isAdminObj == null ? false : Boolean.parseBoolean(isAdminObj.toString().trim()));

		// making appropriate objects
		if (isAdmin) {
			user = GSON.fromJson(body, Administrator.class);
		} else {
			Customer customer = GSON.fromJson(body, Customer.class);
			// new registrations are always unverified until email confirmation
			customer.setState("INACTIVE");
			user = customer;
		} // if-else

		// basic validation
		boolean missingEmail = (user.getEmail() == null || user.getEmail().trim().isEmpty());
		boolean missingPassword = (user.getPassword() == null || user.getPassword().isEmpty());
		if (missingEmail || missingPassword) {
			sendJson(exchange, 400, Map.of("error", "email and password are required"));
			return;
		} // if

		// prevent duplicate accounts
		if (db.userExists(user.getEmail())) {
			sendJson(exchange, 409, Map.of("error", "an account with this email already exists"));
			return;
		} // if

		boolean saved = db.addUser(user);

		if (!saved) {
			sendJson(exchange, 500, Map.of("error", "could not save user"));
			return;
		} // if

		// admins are active immediately; customers must verify by email
		if (isAdmin) {
			sendJson(exchange, 201, user);
			return;
		} // if

		// generate a verification code and "email" it. For this project the
		// email is simulated: the code is printed to the server console and
		// returned so the demo UI can display it.
		String code = generateVerificationCode();
		db.setVerificationCode(user.getEmail(), code);
		System.out.println("[email] Verification code for " + user.getEmail() + ": " + code);

		sendJson(exchange, 201, Map.of("user", user, "verificationCode", code));

		if(!sendEmail(exchange, user, code)) {
			sendJson(exchange, 500, Map.of("error", "could not send verification email"));
			return;
		} // if
    } // handlePostUser

    /**
     * Updates an existing user's profile (first name, last name, mailing address).
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleUpdateUser(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<?, ?> json;

        try {
            json = GSON.fromJson(body, Map.class);
        } catch (Exception e) {
            sendJson(exchange, 400, Map.of("error", "invalid JSON"));
            return;
        } // try-catch

        String email = str(json == null ? null : json.get("email"));
        if (email.isEmpty()) {
            sendJson(exchange, 400, Map.of("error", "email is required"));
            return;
        } // if

        User existing = db.getUser(email);
        if (existing == null) {
            sendJson(exchange, 404, Map.of("error", "user not found"));
            return;
        } // if

        Customer updated = new Customer(
            str(json.get("name")),
            email,
            "",
            str(json.get("lastName")),
            str(json.get("mailingAddress")),
            "ACTIVE"
        );
        updated.setId(existing.getId());

        if (!db.updateUser(updated)) {
            sendJson(exchange, 500, Map.of("error", "could not update user"));
            return;
        } // if

        sendJson(exchange, 200, db.getUser(email));
    } // handleUpdateUser

    /**
     * Handles a user's favorite movies: GET lists them, POST adds one, DELETE removes one.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleFavorites(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        String method = exchange.getRequestMethod();

        if (method.equals("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        } // if

        if (method.equals("GET")) {
            String email = queryEmail(exchange);
            if (email == null) {
                sendJson(exchange, 400, Map.of("error", "email is required"));
                return;
            } // if
            List<Movie> favorites = db.getFavoriteMovies(customerFor(email));
            if (favorites == null) {
                sendJson(exchange, 500, Map.of("error", "could not read favorites"));
                return;
            } // if
            sendJson(exchange, 200, favorites);
            return;
        } // if

        if (!method.equals("POST") && !method.equals("DELETE")) {
            sendJson(exchange, 405, Map.of("error", "method not allowed"));
            return;
        } // if

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<?, ?> json;
        try {
            json = GSON.fromJson(body, Map.class);
        } catch (Exception e) {
            sendJson(exchange, 400, Map.of("error", "invalid JSON"));
            return;
        } // try-catch

        String email = str(json == null ? null : json.get("email"));
        int movieId = intFrom(json == null ? null : json.get("movieId"));
        if (email.isEmpty() || movieId == -1) {
            sendJson(exchange, 400, Map.of("error", "email and movieId are required"));
            return;
        } // if

        User user = db.getUser(email);
        if (user == null) {
            sendJson(exchange, 404, Map.of("error", "user not found"));
            return;
        } // if

        Movie movie = new Movie("", "", "", "", "", 0, false);
        movie.setId(movieId);

        boolean ok = method.equals("POST")
            ? db.addFavoriteMovie(user, movie)
            : db.removeFavoriteMovie(user, movie);

        sendJson(exchange, ok ? 200 : 500, Map.of("ok", ok));
    } // handleFavorites

    /**
     * Handles a user's payment cards: GET lists them, POST adds one (max 3).
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleCards(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        String method = exchange.getRequestMethod();

        if (method.equals("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        } // if

        if (method.equals("GET")) {
            String email = queryEmail(exchange);
            if (email == null) {
                sendJson(exchange, 400, Map.of("error", "email is required"));
                return;
            } // if
            List<Card> cards = db.getCards(customerFor(email));
            if (cards == null) {
                sendJson(exchange, 500, Map.of("error", "could not read cards"));
                return;
            } // if
            List<Map<String, String>> out = new ArrayList<>();
            for (Card card : cards) {
                Map<String, String> m = new HashMap<>();
                m.put("cardNumber", card.getCardNumber());
                m.put("billingAddress", card.getBillingAddress());
                m.put("expirationDate", card.getExpirationDate().toString());
                out.add(m);
            } // for
            sendJson(exchange, 200, out);
            return;
        } // if

        if (!method.equals("POST")) {
            sendJson(exchange, 405, Map.of("error", "method not allowed"));
            return;
        } // if

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<?, ?> json;
        try {
            json = GSON.fromJson(body, Map.class);
        } catch (Exception e) {
            sendJson(exchange, 400, Map.of("error", "invalid JSON"));
            return;
        } // try-catch

        String email = str(json == null ? null : json.get("email"));
        String cardNumber = str(json.get("cardNumber"));
        String billingAddress = str(json.get("billingAddress"));
        String expiration = str(json.get("expirationDate"));

        if (email.isEmpty() || cardNumber.isEmpty() || !expiration.contains("-")) {
            sendJson(exchange, 400, Map.of("error", "email, cardNumber, and expirationDate (YYYY-MM) are required"));
            return;
        } // if

        User user = db.getUser(email);
        if (user == null) {
            sendJson(exchange, 404, Map.of("error", "user not found"));
            return;
        } // if

        List<Card> existing = db.getCards(user);
        if (existing != null && existing.size() >= Customer.MAX_CARDS) {
            sendJson(exchange, 400, Map.of("error", "card limit reached (max " + Customer.MAX_CARDS + ")"));
            return;
        } // if

        String[] parts = expiration.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        Card card = new Card(cardNumber, billingAddress, year, month);

        boolean ok = db.addCard(user, card);
        sendJson(exchange, ok ? 201 : 500, Map.of("ok", ok));
    } // handleCards

    /**
     * Builds a lightweight {@code Customer} carrying only an email, for
     * looking up a user by email in the data layer.
     * @param email The user's email address.
     * @return A {@code Customer} with the given email.
     */
    private static Customer customerFor(String email) {
        return new Customer("", email, "", "", "", "ACTIVE");
    } // customerFor

    /**
     * Reads the {@code email} query parameter, URL-decoded.
     * @param exchange The HTTP exchange.
     * @return The email, or {@code null} if absent.
     */
    private static String queryEmail(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            return null;
        } // if
        String[] parts = query.split("=", 2);
        if (parts.length < 2 || !parts[0].equals("email")) {
            return null;
        } // if
        return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
    } // queryEmail

    /**
     * Returns a trimmed string for a JSON value, or empty if null.
     * @param value The value.
     * @return The trimmed string.
     */
    private static String str(Object value) {
        return value == null ? "" : value.toString().trim();
    } // str

    /**
     * Parses a JSON number into an int, or {@code -1} if invalid.
     * @param value The value.
     * @return The int, or {@code -1}.
     */
    private static int intFrom(Object value) {
        if (value == null) {
            return -1;
        } // if
        try {
            return (int) Double.parseDouble(value.toString().trim());
        } catch (NumberFormatException e) {
            return -1;
        } // try-catch
    } // intFrom
	
	/**
     * Verifies a customer account using an email/code pair from the request
     * body, flipping the account state to ACTIVE when the code matches.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleVerify(HttpExchange exchange) throws IOException {
		// allow the React dev server (different origin) to call this API
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
			return;
		} // if

		if (!method.equals("POST")) {
			sendJson(exchange, 405, Map.of("error", "method not allowed"));
			return;
		} // if

		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		Map<?, ?> json;

		try {
			json = GSON.fromJson(body, Map.class);
		} catch (Exception e) {
			sendJson(exchange, 400, Map.of("error", "invalid JSON"));
			return;
		} // try-catch

		Object emailObj = (json == null ? null : json.get("email"));
		Object codeObj = (json == null ? null : json.get("code"));
		String email = emailObj == null ? "" : emailObj.toString().trim();
		String code = codeObj == null ? "" : codeObj.toString().trim();

		if (email.isEmpty() || code.isEmpty()) {
			sendJson(exchange, 400, Map.of("error", "Email and verification code are required."));
			return;
		} // if

		if (!db.userExists(email)) {
			sendJson(exchange, 404, Map.of("error", "No account found for that email."));
			return;
		} // if

		String storedCode = db.getVerificationCode(email);

		// a null/blank stored code means the account is already verified
		if (storedCode == null || storedCode.isEmpty()) {
			sendJson(exchange, 200, Map.of("message", "Account is already verified. You can log in."));
			return;
		} // if

		if (!storedCode.equals(code)) {
			sendJson(exchange, 400, Map.of("error", "Incorrect verification code."));
			return;
		} // if

		if (!db.activateUser(email)) {
			sendJson(exchange, 500, Map.of("error", "could not verify account"));
			return;
		} // if

		sendJson(exchange, 200, Map.of("message", "Account verified. You can now log in."));
    } // handleVerify

	/**
	 * Sends a verification email to a {@code User} POSTed to this route.
	 * @param exchange The HTTP exchange to respond to.
	 * @param user The user to send the email to.
	 * @param code The user's verification code.
	 * @throws IOException if writing the response fails
	 */
	private static boolean sendEmail(HttpExchange exchange, User user, String code) throws IOException {

		// if the user exists
		if (db.userExists(user.getEmail())) {
			// send the email
			Email email = EmailBuilder.startingBlank()
				.from("Cinema E-booking System", "qwertyshepherd@gmail.com")
				.to(user.getName(), user.getEmail())
				.withSubject("Cinema E-booking System: Your verification code")
				.withPlainText("Hello " + 
					user.getName() + ", your verification code is: " +
					code.toString() + ". Please use this code to finish " +
					"setting up your account.")
				.buildEmail();
			
			Mailer mailer = MailerBuilder
				// i'll actually kill you if you try and use this to get into my account
				.withSMTPServer("smtp.gmail.com", 587, "qwertyshepherd@gmail.com", "cqbw bpvx xtpk befo")
				.withTransportStrategy(TransportStrategy.SMTP_TLS)
				.buildMailer();
			
			mailer.sendMail(email);
			return true;
		} else {
			sendJson(exchange, 500, Map.of("error", "user does not exist"));
			return false;
		} // if-else
    } // sendEmail

    /**
     * Authenticates a user from an email/password pair in the request body.
     * Customers must have a verified (ACTIVE) account to log in.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleLogin(HttpExchange exchange) throws IOException {
		// allow the React dev server (different origin) to call this API
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
			return;
		} // if

		if (!method.equals("POST")) {
			sendJson(exchange, 405, Map.of("error", "method not allowed"));
			return;
		} // if

		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		Map<?, ?> json;

		try {
			json = GSON.fromJson(body, Map.class);
		} catch (Exception e) {
			sendJson(exchange, 400, Map.of("error", "invalid JSON"));
			return;
		} // try-catch

		Object emailObj = (json == null ? null : json.get("email"));
		Object passwordObj = (json == null ? null : json.get("password"));
		String email = emailObj == null ? "" : emailObj.toString().trim();
		String password = passwordObj == null ? "" : passwordObj.toString();

		if (email.isEmpty() || password.isEmpty()) {
			sendJson(exchange, 400, Map.of("error", "Email and password are required."));
			return;
		} // if

		// use the same generic message for unknown email and wrong password
		// so we don't reveal which accounts exist
		if (!db.userExists(email)) {
			sendJson(exchange, 401, Map.of("error", "Incorrect email or password."));
			return;
		} // if

		User user = db.getUser(email);

		if (user == null || !user.getPassword().equals(password)) {
			sendJson(exchange, 401, Map.of("error", "Incorrect email or password."));
			return;
		} // if

		// customers must verify their account (via email) before logging in
		if (!user.isAdmin()) {
			Customer customer = (Customer) user;
			if (customer.getState() == Customer.CustomerState.INACTIVE) {
				sendJson(exchange, 403, Map.of("error",
					"Account is not verified. Please check your email to verify your account."));
				return;
			} // if
			if (customer.getState() == Customer.CustomerState.SUSPENDED) {
				sendJson(exchange, 403, Map.of("error", "This account has been suspended."));
				return;
			} // if
		} // if

		sendJson(exchange, 200, user);
    } // handleLogin

    /**
     * Generates a random 6-digit verification code.
     * @return The zero-padded verification code.
     */
    private static String generateVerificationCode() {
		return String.format("%06d", RANDOM.nextInt(1_000_000));
    } // generateVerificationCode

    /**
     * Writes a JSON response with the given status code.
     * @param exchange The HTTP exchange to respond to.
     * @param status The HTTP status code.
     * @param payload The object to serialize as JSON.
     * @throws IOException if writing the response fails.
     */
    private static void sendJson(HttpExchange exchange, int status, Object payload) throws IOException {
		byte[] bytes = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, bytes.length);

		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		} // try
    } // sendJson
} // App
