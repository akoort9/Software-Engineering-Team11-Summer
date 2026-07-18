package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a reset password request from the frontend. */
public class ResetPasswordRequest extends VerifyRequest {
    /**
     * Checks if a {@code ResetPasswordRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (this.code.isEmpty()) {
            JsonResponse.send(exchange, 400, Map.of("error", "Email and verification code are required."));
			return false;
        } else if (!isResetCodeValid()){
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
	protected boolean isResetCodeValid() {
		String storedCode = db.getResetCode(this.email);
		if (storedCode == null || 
			storedCode.isEmpty() || 
			!storedCode.equals(this.code)) {
			return false;
		} // if
		return db.getResetCodeExpiry(this.email) > System.currentTimeMillis();
	} // isResetCodeValid
} // ResetPasswordRequest
