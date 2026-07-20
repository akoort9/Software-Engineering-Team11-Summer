package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a request to validate a password reset request. */
public class VerifyResetRequest extends UserRequest {
    /** The password reset code. */
    public String code;

    /**
     * Checks if a {@code VerifyResetRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (!isResetCodeValid(exchange)) {
            return false;
        } else {
            return true;
        }// if-else
    } // check

    /**
	 * Checks whether a password-reset code is correct and unexpired for
	 * the given request.
	 * @return {@code true} if the code matches and hasn't expired.
	 */
	protected boolean isResetCodeValid(HttpExchange exchange) throws IOException {
		String storedCode = db.getResetCode(this.email);
		if (storedCode == null || storedCode.isEmpty()) {
            JsonResponse.send(exchange, 200, Map.of("message", "Account password already changed. You can log in."));
			return true;
        } else if (!storedCode.equals(this.code)) {
            JsonResponse.send(exchange, 400, Map.of("error", "Incorrect password reset code."));
			return false;
		} // if-else
		return db.getResetCodeExpiry(this.email) > System.currentTimeMillis();
	} // isResetCodeValid
} // VerifyResetRequest
