package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a request to register a new user. */
public class AddUserRequest extends UpdateUserRequest {
    /** Indicates if the user is an admin or not.  */
    public boolean isAdmin;

    /** The new user's password */
    public String password;

    /** The new user's state. Usually 'INACTIVE' if not an admin. */
    public String state;

    /** Whether or not this new user wants promotional emails. */
    public boolean subscribedToPromotions;

    /**
     * Checks if a {@code UserRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (this.password.isEmpty()) {
            JsonResponse.send(exchange, 400, Map.of("error", "email and password are required"));
            return false;
        } else {
             return true;
        } // if-else
    } // check
} // AddUserRequest
