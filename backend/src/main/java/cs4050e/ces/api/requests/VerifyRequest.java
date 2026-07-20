package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a verification request from the frontend. */
public class VerifyRequest extends UserRequest {
    /** The verification code. */
    public String code;

    /**
     * Checks if a {@code VerifyRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (this.code.isEmpty()) {
            JsonResponse.send(exchange, 400, Map.of("error", "Email and verification code are required."));
			return false;
        } else if (!isVerificationCodeValid(exchange)){
            return false;
        } else {
            return true;
        }// if-else
    } // check

     /**
	 * Checks whether a verification code is correct and unexpired for
	 * the given request.
	 * @return {@code true} if the code matches and hasn't expired.
	 */
	private boolean isVerificationCodeValid(HttpExchange exchange) throws IOException {
		String storedCode = db.getVerificationCode(this.email);
		if (storedCode == null || storedCode.isEmpty()) {
            JsonResponse.send(exchange, 200, Map.of("message", "Account is already verified. You can log in."));
			return true;
        } else if (!storedCode.equals(this.code)) {
			JsonResponse.send(exchange, 400, Map.of("error", "Incorrect verification code."));
			return false;
		} else {
            return true;
        } // if-else
	} // isResetCodeValid
} // VerifyRequest
