package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a request for a movie from the frontend. */
public class MovieRequest implements Request {
    /** The title of the movie this request is for. */
    public String title;

    /**
     * Checks if a {@code MovieRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (title.isEmpty()) {
			JsonResponse.send(exchange, 400, Map.of("error", "title is required"));
			return false;
		} else {
            return true;
        } // if-else
    } // check
} // MovieRequest
