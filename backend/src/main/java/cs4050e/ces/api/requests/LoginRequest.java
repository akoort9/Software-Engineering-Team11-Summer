package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a user's login request. */
public class LoginRequest extends UserRequest {
    /** The password of the user logging in. */
    public String password;

    /**
     * Checks if a {@code LoginRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @Override
    public boolean check(HttpExchange exchange) throws IOException {
        if (this.email.isEmpty()) {
            JsonResponse.send(exchange, 400, Map.of("error", "email is required"));
            return false;
        } else if (!db.userExists(email) ||
            !db.getUser(this.email).getPassword().equals(db.hashPassword(this.password))) {
            // use the same generic message for unknown email and wrong password
		    // so we don't reveal which accounts exist
            JsonResponse.send(exchange, 401, Map.of("error", "Incorrect email or password."));
            return false;
        } else if (this.password.isEmpty()) {
            JsonResponse.send(exchange, 400, Map.of("error", "Email and password are required."));
            return false;
        } else {
            return true;
        } // if-else
    } // check
} // LoginRequest
