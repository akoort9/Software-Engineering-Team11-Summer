package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a new password request from the frontend. */
public class NewPasswordRequest extends VerifyResetRequest {
    /** The new account password. */
    public String newPassword;

    /**
     * Checks if a {@code NewPasswordRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (this.newPassword.isEmpty()){
            JsonResponse.send(exchange, 400, Map.of("error", "Email, code, and new password are required."));
            return false;
        } else if (this.newPassword.length() < 8) {
            JsonResponse.send(exchange, 400, Map.of("error", "Password must be at least 8 characters."));
			return false;
        } else {
            return true;
        }// if-else
    } // check
} // NewPasswordRequest
