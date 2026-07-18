package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a request about a user's favorite movie. */
public class FavoriteMovieRequest extends UserRequest {
    /** The database id of the movie to favorite. */
    public int movieId;

    /**
     * Checks if a {@code FavoriteMovieRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (movieId == -1) {
            JsonResponse.send(exchange, 400, Map.of("error", "email and movieId are required"));
            return false;
        } else {
            return true;
        } // if-else
    } // check
} // FavoriteMovieRequest
