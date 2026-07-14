package cs4050e.ces;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import cs4050e.ces.db.DataHandler;
import cs4050e.ces.db.theatre.Movie;
import cs4050e.ces.db.users.*;

/** Runs the HTTP API that the React frontend talks to. */
public class App {
    /** Port the server listens on. */
    private static final int PORT = 8080;

    /** Connection to the database. */
    private static final DataHandler db = DataHandler.getInstance();

    /** GSON object. */
    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Starts the HTTP server.
     * @param args unused.
     * @throws IOException if the server fails to start.
     */
    public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

		server.createContext("/api/movies", App::handleMovies);
		server.createContext("/api/user", App::handleUsers);
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
		boolean isAdmin = (isAdminObj == null ? false : Boolean.getBoolean(isAdminObj.toString().trim()));

		// making appropriate objects
		if (isAdmin) {
			user = GSON.fromJson(body, Administrator.class);
		} else {
			user = GSON.fromJson(body, Customer.class);
		} // if-else
		
		boolean saved = db.addUser(user);

		if (!saved) {
			sendJson(exchange, 500, Map.of("error", "could not save user"));
			return;
		} // if

		sendJson(exchange, 201, user);
    } // handlePostUser

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
