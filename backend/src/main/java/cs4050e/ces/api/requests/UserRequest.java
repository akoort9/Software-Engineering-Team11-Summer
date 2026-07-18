package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.*;
import cs4050e.ces.db.DataHandler;


/** Represents a generic request about a {@code User}
 * from the frontend.
 */
public class UserRequest {
    /** Database access point. */
    protected static final DataHandler db = DataHandler.getInstance();

    /** The email of the user this request is for. */
    public String email;

    /**
     * Checks if a {@code UserRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        // no empty requests
        if (email.isEmpty()) {
            JsonResponse.send(exchange, 400, Map.of("error", "email is required"));
            return false;
        } else if (!db.userExists(email)) {
            JsonResponse.send(exchange, 404, Map.of("error", "user not found"));
            return false;
        } else {
            return true;
        } // if-else
    } // check
} // UserRequest
