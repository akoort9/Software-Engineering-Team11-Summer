package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;
import cs4050e.ces.db.DataHandler;
import cs4050e.ces.db.theatre.Movie;

/** Represents a request for a movie from the frontend. */
public class MovieRequest implements Request {
    /** Database access point. */
    protected static final DataHandler db = DataHandler.getInstance();

    /** The admin submitting this request. */
    protected String adminEmail;

    /** The movie this request is for. */
    public Movie movie;

    /**
     * Checks if a {@code MovieRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!db.userExists(adminEmail)) {
            JsonResponse.send(exchange, 404, Map.of("error", "user not found"));
            return false;
        } else if (!db.getUser(adminEmail).isAdmin()) {
            JsonResponse.send(exchange, 403, Map.of("error", "requires administrator access"));
            return false;
        } else if (this.movie.getTitle().isEmpty()) {
			JsonResponse.send(exchange, 400, Map.of("error", "title is required"));
			return false;
		} else {
            return true;
        } // if-else
    } // check
} // MovieRequest
