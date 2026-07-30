package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents an admin request to change a customer's account state. */
public class UserStateRequest extends AdminRequest {
    /** The email address of the customer whose state is changing. */
    public String email;

    /** The new state (ACTIVE, INACTIVE or SUSPENDED). */
    public String state;

    /**
     * Checks that the target user and requested state are valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @Override
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (email == null || !db.userExists(email)) {
            JsonResponse.send(exchange, 404, Map.of("error", "target user not found"));
            return false;
        } else if (db.getUser(email).isAdmin()) {
            JsonResponse.send(exchange, 400, Map.of("error", "cannot change an administrator's state"));
            return false;
        } else if (!state.equals("ACTIVE") && !state.equals("INACTIVE") && !state.equals("SUSPENDED")) {
            JsonResponse.send(exchange, 400, Map.of("error", "invalid state"));
            return false;
        } else {
            return true;
        } // if-else
    } // check
} // UserStateRequest
