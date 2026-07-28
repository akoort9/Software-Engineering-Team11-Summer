package cs4050e.ces;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs4050e.ces.api.requests.*;
import cs4050e.ces.api.responses.*;
import cs4050e.ces.db.DataHandler;
import cs4050e.ces.db.theatre.Movie;
import cs4050e.ces.db.theatre.Seat;
import cs4050e.ces.db.theatre.Showroom;
import cs4050e.ces.db.theatre.Showtime;
import cs4050e.ces.db.payment.Card;
import cs4050e.ces.db.payment.Promotion;
import cs4050e.ces.db.payment.Ticket;
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

    /** How long a password-reset code stays valid after it's sent. */
    private static final long RESET_CODE_TTL_MILLIS = 15 * 60 * 1000;

    /**
     * Starts the HTTP server.
     * @param args unused.
     * @throws IOException if the server fails to start.
     */
    public static void main(String[] args) throws IOException {
		// check args
		for (String arg: args) {
			switch(arg) {
				case "--fresh":
					db.wipe();
					System.out.println("Seeded empty database.");
					break;
				default:
					break;
			} // switch
		} // for

		HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

		server.createContext("/api/movies", App::handleMovies);
		server.createContext("/api/showtimes", App::handleShowtimes);
		server.createContext("/api/showrooms", App::handleShowrooms);
		server.createContext("/api/seats", App::handleSeats);
		server.createContext("/api/bookings", App::handleBookings);
		server.createContext("/api/user", App::handleUsers);
		server.createContext("/api/users", App::handleAdminUsers);
		server.createContext("/api/promotions", App::handlePromotions);
		server.createContext("/api/favorites", App::handleFavorites);
		server.createContext("/api/cards", App::handleCards);
		server.createContext("/api/login", App::handleLogin);
		server.createContext("/api/verify", App::handleVerify);
		server.createContext("/api/forgot-password", App::handleForgotPassword);
		server.createContext("/api/verify-reset-code", App::handleVerifyResetCode);
		server.createContext("/api/reset-password", App::handleResetPassword);
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
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
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
			JsonResponse.send(exchange, 500, Map.of("error", "could not read database"));
			return;
		} else {
			JsonResponse.send(exchange, 200, movies);
		} // if-else
    } // handleGetMovies

    /**
     * Reads a movie title from the request body, builds a {@code Movie}
     * (other fields default to empty), and stores it in the database.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handlePostMovie(HttpExchange exchange) throws IOException {
		// handle request
		MovieRequest request = GSON.fromJson(getBody(exchange), MovieRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		boolean saved = db.addMovie(request.movie);

		if (!saved) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not save movie"));
			return;
		} // if

		JsonResponse.send(exchange, 201, request.movie);
    } // handlePostMovie

	/**
     * Routes requests to {@code /api/showtimes} based on HTTP method.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleShowtimes(HttpExchange exchange) throws IOException {
		// allow the React dev server (different origin) to call this API
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
		} // if
		else if (method.equals("GET")) {
			handleGetShowtimes(exchange);
		} // elif
		else if (method.equals("POST")) {
			handlePostShowtime(exchange);
		} // elif
		else {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
		} // else
    } // handleShowtimes

	/**
     * Returns showtimes.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleGetShowtimes(HttpExchange exchange) throws IOException {
		Map<String, String> query = queryParams(exchange);
		String movieIdParam = query.get("movieId");

		List<Showtime> showtimes;
		if (movieIdParam == null) {
			showtimes = db.getShowtimes();
		} else {
			try {
				showtimes = db.getShowtimesForMovie(Integer.parseInt(movieIdParam));
			} catch (NumberFormatException nfe) {
				JsonResponse.send(exchange, 400, Map.of("error", "invalid movieId"));
				return;
			} // try-catch
		} // if-else

		if (showtimes == null) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not read database"));
			return;
		} // if

		List<Map<String, Object>> out = new ArrayList<>();
		for (Showtime showtime : showtimes) {
			Map<String, Object> m = new HashMap<>();
			m.put("id", showtime.getId());
			m.put("movieId", showtime.getMovieId());
			m.put("showroomId", showtime.getShowroomId());
			m.put("startTime", showtime.getStartTime().toInstant().toString());
			m.put("endTime", showtime.getEndTime().toInstant().toString());
			out.add(m);
		} // for

		JsonResponse.send(exchange, 200, out);
    } // handleGetShowtimes

	/**
     * Adds showtimes.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handlePostShowtime(HttpExchange exchange) throws IOException {
		// handle request
		ScheduleMovieRequest request = GSON.fromJson(getBody(exchange), ScheduleMovieRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		try {
			if (db.hasShowtimeConflict(request.showroomID, request.getStartTime(), request.getEndTime())) {
				JsonResponse.send(exchange, 409, Map.of("error",
					"that showroom already has a showtime scheduled during this time"));
				return;
			} // if

			Showtime showtime = new Showtime(
			db.resolveMovieId(request.movie),
			request.showroomID,
			request.getStartTime(),
			request.getEndTime());

			if (!db.addShowtime(showtime)) {
				JsonResponse.send(exchange, 500, Map.of("error", "could not schedule movie."));
				return;
			} // if

			JsonResponse.send(exchange, 201, showtime);
		} catch (ParseException pe) {
			JsonResponse.send(exchange, 400, Map.of("error", "invalid format"));
			return;
		} // try-catch
    } // handlePostShowtimes

	/**
     * Routes requests to {@code /api/showrooms} based on HTTP method.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleShowrooms(HttpExchange exchange) throws IOException {
		// allow the React dev server (different origin) to call this API
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
		} // if
		else if (method.equals("GET")) {
			handleGetShowrooms(exchange);
		} // elif
		else {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
		} // else
    } // handleShowrooms

	/**
     * Returns showrooms.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleGetShowrooms(HttpExchange exchange) throws IOException {
		List<Showroom> showrooms = db.getShowrooms();

		if (showrooms == null) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not read database"));
			return;
		} // if

		JsonResponse.send(exchange, 200, showrooms);
    } // handleGetShowrooms

	/**
     * Routes requests to {@code /api/seats} based on HTTP method.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleSeats(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
			return;
		} else if (!method.equals("GET")) {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
			return;
		} // if-elif

		handleGetSeats(exchange);
    } // handleSeats

	/**
     * Returns every seat for a showtime's showroom, each flagged with
     * whether it's already booked for that showtime.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleGetSeats(HttpExchange exchange) throws IOException {
		Map<String, String> query = queryParams(exchange);
		String showtimeIdParam = query.get("showtimeId");

		if (showtimeIdParam == null) {
			JsonResponse.send(exchange, 400, Map.of("error", "showtimeId is required"));
			return;
		} // if

		int showtimeId;
		try {
			showtimeId = Integer.parseInt(showtimeIdParam);
		} catch (NumberFormatException nfe) {
			JsonResponse.send(exchange, 400, Map.of("error", "invalid showtimeId"));
			return;
		} // try-catch

		Showtime showtime = db.getShowtime(showtimeId);
		if (showtime == null) {
			JsonResponse.send(exchange, 404, Map.of("error", "showtime not found"));
			return;
		} // if

		List<Seat> seats = db.getSeats(showtime.getShowroomId());
		List<Integer> booked = db.getBookedSeatIds(showtimeId);

		if (seats == null || booked == null) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not read database"));
			return;
		} // if

		List<Map<String, Object>> out = new ArrayList<>();
		for (Seat seat : seats) {
			Map<String, Object> m = new HashMap<>();
			m.put("id", seat.getId());
			m.put("rowLabel", seat.getRowLabel());
			m.put("seatNumber", seat.getSeatNumber());
			m.put("label", seat.getLabel());
			m.put("booked", booked.contains(seat.getId()));
			out.add(m);
		} // for

		JsonResponse.send(exchange, 200, out);
    } // handleGetSeats

	/**
     * Routes requests to {@code /api/bookings} based on HTTP method.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleBookings(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
			return;
		} else if (!method.equals("POST")) {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
			return;
		} // if-elif

		handlePostBooking(exchange);
    } // handleBookings

	/**
     * Books seats for a showtime. This sprint doesn't implement real-time
     * seat locking, so availability is checked right before booking and the
     * {@code tickets} table's unique (showtime_id, seat_id) constraint is
     * the final backstop against double-booking a seat.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handlePostBooking(HttpExchange exchange) throws IOException {
		BookTicketsRequest request = GSON.fromJson(getBody(exchange), BookTicketsRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		User user = db.getUser(request.email);
		Showtime showtime = db.getShowtime(request.showtimeId);

		List<Integer> alreadyBooked = db.getBookedSeatIds(request.showtimeId);
		List<Integer> conflicts = new ArrayList<>();
		for (BookTicketsRequest.SeatSelection selection : request.seats) {
			if (alreadyBooked.contains(selection.seatId)) {
				conflicts.add(selection.seatId);
			} // if
		} // for

		if (!conflicts.isEmpty()) {
			JsonResponse.send(exchange, 409,
				Map.of("error", "some seats are already booked", "seatIds", conflicts));
			return;
		} // if

		java.sql.Date purchaseDate = new java.sql.Date(System.currentTimeMillis());
		List<Ticket> tickets = new ArrayList<>();
		for (BookTicketsRequest.SeatSelection selection : request.seats) {
			Ticket.TicketType type = Ticket.TicketType.valueOf(selection.ticketType.toUpperCase());
			tickets.add(new Ticket(user.getId(), request.showtimeId, selection.seatId, type, purchaseDate));
		} // for

		List<Ticket> booked = db.bookSeats(tickets);
		if (booked == null) {
			JsonResponse.send(exchange, 409,
				Map.of("error", "one or more seats were just booked by someone else"));
			return;
		} // if

		Map<Integer, String> seatLabels = new HashMap<>();
		for (Seat seat : db.getSeats(showtime.getShowroomId())) {
			seatLabels.put(seat.getId(), seat.getLabel());
		} // for

		double total = 0;
		List<Map<String, Object>> ticketsOut = new ArrayList<>();
		for (Ticket ticket : booked) {
			Map<String, Object> t = new HashMap<>();
			t.put("id", ticket.getId());
			t.put("seatId", ticket.getSeatId());
			t.put("seatLabel", seatLabels.get(ticket.getSeatId()));
			t.put("ticketType", ticket.getTypeString());
			t.put("price", ticket.getPrice());
			ticketsOut.add(t);
			total += ticket.getPrice();
		} // for

		// Retrieve movie and showroom details for the email
		Movie movie = db.getMovie(showtime.getMovieId());
		String movieTitle = movie != null ? movie.getTitle() : "Unknown Movie";

		String showroomName = "Unknown Showroom";
		List<Showroom> showrooms = db.getShowrooms();
		if (showrooms != null) {
			for (Showroom sr : showrooms) {
				if (sr.getId() == showtime.getShowroomId()) {
					showroomName = sr.getName();
					break;
				}
			}
		}

		String showtimeDate = showtime.getStartTime().toString();

		// Send email confirmation
		EmailResponse.sendTickets(user, booked.size(), total, ticketsOut, movieTitle, showroomName, showtimeDate);

		JsonResponse.send(exchange, 201, Map.of(
			"tickets", ticketsOut,
			"totalPrice", total,
			"showtimeId", request.showtimeId,
			"purchaseDate", purchaseDate.toString()
		));
	} // handlePostBooking

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
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
		} // else
    } // handleUsers

	/**
     * Returns a given user stored in the database as JSON.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleGetUser(HttpExchange exchange) throws IOException {
		// extract email from query
		String email = queryEmail(exchange);

		// user with email does not exist
		if (!db.userExists(email)) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not find user"));
			return;
		} // if

		JsonResponse.send(exchange, 200, db.getUser(email));
    } // handleGetUser

	/**
     * Reads a user from the request body, builds a {@code User},
     * and stores it in the database.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handlePostUser(HttpExchange exchange) throws IOException {
		// handle request
		AddUserRequest request = GSON.fromJson(getBody(exchange), AddUserRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if
		User user = null;

		// making appropriate objects
		if (request.isAdmin) {
			user = new Administrator(
				request.name, 
				request.email, 
				request.password
			);
		} else {
			user = new Customer(
				request.name,
				request.email,
				request.password,
				request.lastName, 
				request.mailingAddress,
			"INACTIVE"
			);
		} // if-else

		boolean saved = db.addUser(user);

		if (!saved) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not save user"));
			return;
		} // if

		if (request.isAdmin) {
			// admins are active immediately; customers must verify by email
			JsonResponse.send(exchange, 201, user);
			return;
		} // if

		// generate a verification code and email it.
		String code = generateVerificationCode();
		db.setVerificationCode(user.getEmail(), code);
		System.out.println("[email] Verification code for " + user.getEmail() + ": " + code);

		JsonResponse.send(exchange, 201, Map.of("user", user, "verificationCode", code));

		if(!EmailResponse.send(EmailTemplates.Template.VERIFICATION, user, code, null)) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not send verification email"));
			return;
		} // if
    } // handlePostUser

    /**
     * Updates an existing user's profile (first name, last name, mailing address).
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleUpdateUser(HttpExchange exchange) throws IOException {
		UpdateUserRequest request = GSON.fromJson(getBody(exchange), UpdateUserRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		Customer updated = new Customer(
			request.name,
			request.email,
			"",
			request.lastName,
			request.mailingAddress,
			"ACTIVE"
		);

		if (!db.updateUser(updated)) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not update user"));
			return;
		} // if

		JsonResponse.send(exchange, 200, db.getUser(request.email));

		if (!EmailResponse.send(EmailTemplates.Template.ACCOUNT_UPDATED, updated, null, null)) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not send account-update email"));
			return;
		} // if
	} // handleUpdateUser

	/**
	 * Routes admin requests to {@code /api/users} based on HTTP method.
	 * @param exchange The HTTP exchange to respond to.
	 * @throws IOException if writing the response fails.
	 */
	private static void handleAdminUsers(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, PUT, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
		} // if
		else if (method.equals("GET")) {
			handleGetUsers(exchange);
		} // elif
		else if (method.equals("PUT")) {
			handleSetUserState(exchange);
		} // elif
		else {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
		} // else
	} // handleAdminUsers

	/**
	 * Returns every user as JSON, for the admin user-management view.
	 * Requires an admin's email as the {@code email} query parameter.
	 * @param exchange The HTTP exchange to respond to.
	 * @throws IOException if writing the response fails.
	 */
	private static void handleGetUsers(HttpExchange exchange) throws IOException {
		String adminEmail = queryEmail(exchange);
		if (adminEmail == null || !db.userExists(adminEmail) || !db.getUser(adminEmail).isAdmin()) {
			JsonResponse.send(exchange, 403, Map.of("error", "requires administrator access"));
			return;
		} // if

		List<User> users = db.getUsers();
		if (users == null) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not read database"));
			return;
		} // if

		List<Map<String, Object>> out = new ArrayList<>();
		for (User user : users) {
			Map<String, Object> m = new HashMap<>();
			m.put("id", user.getId());
			m.put("name", user.getName());
			m.put("email", user.getEmail());
			if (user.isAdmin()) {
				m.put("role", "admin");
				m.put("lastName", "");
				m.put("state", "ACTIVE");
				m.put("subscribedToPromotions", false);
			} else {
				Customer customer = (Customer) user;
				m.put("role", "customer");
				m.put("lastName", customer.getLastName());
				m.put("state", customer.getState().toString());
				m.put("subscribedToPromotions", customer.isSubscribedToPromotions());
			} // if-else
			out.add(m);
		} // for

		JsonResponse.send(exchange, 200, out);
	} // handleGetUsers

	/**
	 * Changes a customer's account state (activate/suspend) on an admin's request.
	 * @param exchange The HTTP exchange to respond to.
	 * @throws IOException if writing the response fails.
	 */
	private static void handleSetUserState(HttpExchange exchange) throws IOException {
		UserStateRequest request = GSON.fromJson(getBody(exchange), UserStateRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		if (!db.setUserState(request.email, request.state)) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not update user state"));
			return;
		} // if

		JsonResponse.send(exchange, 200, Map.of("ok", true));
	} // handleSetUserState

	/**
	 * Routes admin requests to {@code /api/promotions} based on HTTP method.
	 * @param exchange The HTTP exchange to respond to.
	 * @throws IOException if writing the response fails.
	 */
	private static void handlePromotions(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
		} // if
		else if (method.equals("GET")) {
			handleGetPromotions(exchange);
		} // elif
		else if (method.equals("POST")) {
			handlePostPromotion(exchange);
		} // elif
		else {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
		} // else
	} // handlePromotions

	/**
	 * Returns every promotion as JSON, for the admin promotions view.
	 * Requires an admin's email as the {@code email} query parameter.
	 * @param exchange The HTTP exchange to respond to.
	 * @throws IOException if writing the response fails.
	 */
	private static void handleGetPromotions(HttpExchange exchange) throws IOException {
		String adminEmail = queryEmail(exchange);
		if (adminEmail == null || !db.userExists(adminEmail) || !db.getUser(adminEmail).isAdmin()) {
			JsonResponse.send(exchange, 403, Map.of("error", "requires administrator access"));
			return;
		} // if

		List<Promotion> promotions = db.getPromotions();
		if (promotions == null) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not read database"));
			return;
		} // if

		List<Map<String, Object>> out = new ArrayList<>();
		for (Promotion promo : promotions) {
			Map<String, Object> m = new HashMap<>();
			m.put("id", promo.getId());
			m.put("promoCode", promo.getPromoCode());
			m.put("percentOff", (int) Math.round((1.0 - promo.getDiscountPercent()) * 100));
			m.put("expirationDate", promo.getExpirationDate().toString());
			out.add(m);
		} // for

		JsonResponse.send(exchange, 200, out);
	} // handleGetPromotions

	/**
	 * Creates a promotion and emails it to every customer subscribed to
	 * promotional offers.
	 * @param exchange The HTTP exchange to respond to.
	 * @throws IOException if writing the response fails.
	 */
	private static void handlePostPromotion(HttpExchange exchange) throws IOException {
		PromotionRequest request = GSON.fromJson(getBody(exchange), PromotionRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		Promotion promotion = new Promotion(
			request.promoCode.trim(),
			request.getDiscountMultiplier(),
			request.getExpirationDate());

		if (!db.addPromotion(promotion)) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not save promotion"));
			return;
		} // if

		// email every subscribed customer
		int sent = 0;
		List<User> users = db.getUsers();
		if (users != null) {
			for (User user : users) {
				if (user.isAdmin() || !((Customer) user).isSubscribedToPromotions()) {
					continue;
				} // if
				try {
					if (EmailResponse.sendPromotion(user, promotion.getPromoCode(),
							request.percentOff, promotion.getExpirationDate())) {
						sent++;
					} // if
				} catch (Exception e) {
					System.err.println("handlePostPromotion (email): " + e);
				} // try-catch
			} // for
		} // if

		JsonResponse.send(exchange, 201,
			Map.of("promoCode", promotion.getPromoCode(), "emailsSent", sent));
	} // handlePostPromotion

    /**
     * Handles a user's favorite movies: 
	 * GET lists them, 
	 * POST adds one, 
	 * DELETE removes one.
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
        } else if (method.equals("GET")) {
            handleGetFavorites(exchange);
        } else if (method.equals("POST") || method.equals("DELETE")) {
			handlePostFavorites(exchange);
		} else {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
            return;
		} // if-else
    } // handleFavorites

	/**
     * Handles getting a user's favorite movies.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
	private static void handleGetFavorites(HttpExchange exchange) throws IOException {
		String email = queryEmail(exchange);
        if (email == null) {
            JsonResponse.send(exchange, 400, Map.of("error", "email is required"));
            return;
        } // if

        List<Movie> favorites = db.getFavoriteMovies(customerFor(email));

        if (favorites == null) {
            JsonResponse.send(exchange, 500, Map.of("error", "could not read favorites"));
            return;
        } // if

        JsonResponse.send(exchange, 200, favorites);
	} // handleGetFavorites

	/**
     * Handles adding or removing a user's favorite movies.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
	private static void handlePostFavorites(HttpExchange exchange) throws IOException {
		// handle request
		FavoriteMovieRequest request = GSON.fromJson(getBody(exchange), FavoriteMovieRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		User user = db.getUser(request.email);
        Movie movie = db.getMovie(request.movieId);

        boolean ok = exchange.getRequestMethod().equals("POST")
            ? db.addFavoriteMovie(user, movie)
            : db.removeFavoriteMovie(user, movie);

        JsonResponse.send(exchange, ok ? 200 : 500, Map.of("ok", ok));
	} // handlePostFavorites

    /**
     * Handles a user's payment cards: 
	 * GET lists them, 
	 * POST adds one (max 3),
	 * DELETE removes one,
	 * PUT updates one.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleCards(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        String method = exchange.getRequestMethod();

        if (method.equals("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        } else if (method.equals("GET")) {
			handleGetCards(exchange);
		} else if (method.equals("POST") ||
			method.equals("PUT") ||
			method.equals("DELETE")) {
			handlePostCards(exchange);
		} else {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
            return;
		} // if-else
    } // handleCards

	/**
     * Handles getting a user's payment cards: 
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handleGetCards(HttpExchange exchange) throws IOException {
		String email = queryEmail(exchange);
        if (email == null) {
            JsonResponse.send(exchange, 400, Map.of("error", "email is required"));
            return;
        } // if

        List<Card> cards = db.getCards(customerFor(email));

        if (cards == null) {
            JsonResponse.send(exchange, 500, Map.of("error", "could not read cards"));
            return;
        } // if

        List<Map<String, String>> out = new ArrayList<>();
            
		for (Card card : cards) {
            Map<String, String> m = new HashMap<>();
            m.put("id", String.valueOf(card.getId()));
            m.put("cardNumber", card.getCardNumber());
            m.put("billingAddress", card.getBillingAddress());
            m.put("expirationDate", card.getExpirationDate().toString());
            out.add(m);
	    } // for
            
		JsonResponse.send(exchange, 200, out);  
		return;
	} // handleGetCards

	/**
     * Handles adding, updating or removing a user's payment cards.
     * @param exchange The HTTP exchange to respond to.
     * @throws IOException if writing the response fails.
     */
    private static void handlePostCards(HttpExchange exchange) throws IOException {
		String method = exchange.getRequestMethod();
		UpdateCardRequest request = null;

		String body = getBody(exchange);

		// DELETE sends a simple CardRequest
		if (method.equals("DELETE")) {
			CardRequest deleteRequest = GSON.fromJson(body, CardRequest.class);
			if (!checkRequest(exchange, deleteRequest)) { return; } // if

			User deleteUser = db.getUser(deleteRequest.email);
			Card cardToRemove = db.getCards(deleteUser).stream()
				.filter(c -> c.getId() == deleteRequest.cardId)
				.findFirst()
				.orElse(null);

			boolean removed = db.removeCard(deleteUser, deleteRequest.cardId);
			JsonResponse.send(exchange, removed ? 200 : 500, Map.of("ok", removed));

			if (removed && cardToRemove != null
					&& !EmailResponse.send(EmailTemplates.Template.CARD_REMOVED, deleteUser, cardToRemove)) {
				JsonResponse.send(exchange, 500, Map.of("error", "could not send card-removed email"));
				return;
			} // if
			return;
		} else {
			request = GSON.fromJson(body, UpdateCardRequest.class);
			if (!checkRequest(exchange, request)) { return; } // if
		} // if-else

		User user = db.getUser(request.email);

		// POST and PUT both need card details 
        String[] parts = request.expirationDate.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        Card card = new Card(request.cardNumber, request.billingAddress, year, month);

        // update an existing card (PUT)
		if (exchange.getRequestMethod().equals("PUT")) {
			boolean updated = db.updateCard(user, request.cardId, card);
			JsonResponse.send(exchange, updated ? 200 : 500, Map.of("ok", updated));

			if (updated && !EmailResponse.send(EmailTemplates.Template.CARD_UPDATED, user, card)) {
				JsonResponse.send(exchange, 500, Map.of("error", "could not send card-updated email"));
				return;
			} // if
			return;
		} // if

        // add a new card (POST)
		List<Card> existing = db.getCards(user);
		if (existing != null && existing.size() >= Customer.MAX_CARDS) {
			JsonResponse.send(exchange, 400, Map.of("error", "card limit reached (max " + Customer.MAX_CARDS + ")"));
			return;
		} // if

		boolean ok = db.addCard(user, card);
		JsonResponse.send(exchange, ok ? 201 : 500, Map.of("ok", ok));

		if (ok && !EmailResponse.send(EmailTemplates.Template.CARD_ADDED, user, card)) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not send card-added email"));
			return;
		} // if
	} // handlePostCards

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
     * Parses all query parameters off a request's URI.
     * @param exchange The HTTP exchange.
     * @return A {@code Map} of query parameter names to values.
     */
    private static Map<String, String> queryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            return params;
        } // if

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                params.put(
                    URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                );
            } // if
        } // for

        return params;
    } // queryParams

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
		} else if (!method.equals("POST")) {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
			return;
		} // if

		// handle request
		VerifyRequest request = GSON.fromJson(getBody(exchange), VerifyRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		if (!db.activateUser(request.email)) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not verify account"));
			return;
		} // if

		JsonResponse.send(exchange, 200, Map.of("message", "Account verified. You can now log in."));
    } // handleVerify

	/**
	 * Starts a password-reset flow for an email/request body. Always
	 * responds with a generic message, whether or not an account exists
	 * for that email, so the response can't be used to test which emails
	 * are registered.
	 * @param exchange The HTTP exchange to respond to.
	 * @throws IOException if writing the response fails.
	 */
    private static void handleForgotPassword(HttpExchange exchange) throws IOException {
		// allow the React dev server (different origin) to call this API
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
			return;
		} else if (!method.equals("POST")) {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
			return;
		} // if-else

		// handle request
		UserRequest request = GSON.fromJson(getBody(exchange), UserRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		User user = db.getUser(request.email);

		// generate and store verification code
		String code = generateVerificationCode();
		long expiresAt = System.currentTimeMillis() + RESET_CODE_TTL_MILLIS;
		db.setResetCode(request.email, code, expiresAt);
		System.out.println("[email] Password reset code for " + request.email + ": " + code);

		// sending the email
		if (!EmailResponse.send(EmailTemplates.Template.PASSWORD_RESET, user, code, null)) {
			System.err.println("[email] failed to send password reset email to " + request.email);
		} // if

		// same response regardless of whether the account exists
		JsonResponse.send(exchange, 200,
			Map.of("message", "If an account exists for that email, a password reset code has been sent."));
    } // handleForgotPassword

	/**
	 * Confirms an email/code pair from a password-reset flow without
	 * changing any account state, so the frontend can move the user on to
	 * the "choose a new password" step.
	 * @param exchange The HTTP exchange to respond to.
	 * @throws IOException if writing the response fails.
	 */
    private static void handleVerifyResetCode(HttpExchange exchange) throws IOException {
		// allow the React dev server (different origin) to call this API
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
			return;
		} else if (!method.equals("POST")) {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
			return;
		} // if

		// handle request
		VerifyResetRequest request = GSON.fromJson(getBody(exchange), VerifyResetRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		JsonResponse.send(exchange, 200, Map.of("message", "Code verified."));
    } // handleVerifyResetCode

	/**
	 * Completes a password-reset flow: re-checks the email/code pair from
	 * the request body and, if still valid, sets the account's new
	 * password and clears the reset code.
	 * @param exchange The HTTP exchange to respond to.
	 * @throws IOException if writing the response fails.
	 */
    private static void handleResetPassword(HttpExchange exchange) throws IOException {
		// allow the React dev server (different origin) to call this API
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

		String method = exchange.getRequestMethod();

		if (method.equals("OPTIONS")) {
			exchange.sendResponseHeaders(204, -1);
			return;
		} else if (!method.equals("POST")) {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
			return;
		} // if

		// handle request
		NewPasswordRequest request = GSON.fromJson(getBody(exchange), NewPasswordRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		// update the password
		if (!db.updatePassword(request.email, request.newPassword)) {
			JsonResponse.send(exchange, 500, Map.of("error", "could not reset password"));
			return;
		} // if

		JsonResponse.send(exchange, 200, Map.of("message", "Password reset. You can now log in."));
    } // handleResetPassword

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
		} else if (!method.equals("POST")) {
			JsonResponse.send(exchange, 405, Map.of("error", "method not allowed"));
			return;
		} // if

		// handle request
		LoginRequest request = GSON.fromJson(getBody(exchange), LoginRequest.class);
		if (!checkRequest(exchange, request)) { return; } // if

		User user = db.getUser(request.email);

		// customers must verify their account (via email) before logging in
		if (!user.isAdmin()) {
			Customer customer = (Customer) user;
			if (customer.getState() == Customer.CustomerState.INACTIVE) {
				JsonResponse.send(exchange, 403, Map.of("error",
					"Account is not verified. Please check your email to verify your account."));
				return;
			} // if
			if (customer.getState() == Customer.CustomerState.SUSPENDED) {
				JsonResponse.send(exchange, 403, Map.of("error", "This account has been suspended."));
				return;
			} // if
		} // if

		JsonResponse.send(exchange, 200, user);
    } // handleLogin

	/**
	 * Checks a {@code Request} from the frontend.
	 * @param request The request.
	 * @param exchange The HTTP exchange to respond to.
	 * @return {@code true} if successful, {@code false} otherwise.
	 */
	private static boolean checkRequest(HttpExchange exchange, Request request) {
		if (exchange == null || request == null) {
			System.err.println(getCallerMethodName() + ": cannot check with null arguments.");
			return false;
		} // if

		try {
			if (!request.check(exchange)) {
				System.err.println(getCallerMethodName() + ": failed request.");
				return false;
			} else {
				return true;
			}// if-else
		} catch (JsonSyntaxException jse) {
			try { JsonResponse.send(exchange, 400, Map.of("error", "invalid JSON")); }
			catch (IOException ioe) { System.err.println(getCallerMethodName() + ": failed to send error."); }
			return false;
		} catch (IOException ioe) {
			System.err.println(getCallerMethodName() + ": " + ioe);
			return false;
		} // try-catch
	} // checkUserRequest

	/**
	 * Source - https://stackoverflow.com/a/68674306
	 * Posted by Nathan
	 * Retrieved 2026-07-19, License - CC BY-SA 4.0
	 * Returns the name of the calling method.
	 * @return caller method name. 
	 */
	private static String getCallerMethodName() {
		return StackWalker.
			getInstance().
			walk(stream -> stream.skip(2).findFirst().get()).
			getMethodName();
	} // getCallerMethodName

	/**
	 * Gets the body of the {@code HttpExchange}.
	 * @param exchange The HTTP exchange.
	 * @return The body of the exchange.
	 */
	private static String getBody(HttpExchange exchange) throws IOException {
		return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
	} // getBody

    /**
     * Generates a random 6-digit verification code.
     * @return The zero-padded verification code.
     */
    private static String generateVerificationCode() {
		return String.format("%06d", RANDOM.nextInt(1_000_000));
    } // generateVerificationCode
} // App
