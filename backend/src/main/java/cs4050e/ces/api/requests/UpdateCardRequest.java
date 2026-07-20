package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a request for a user's card. */
public class UpdateCardRequest extends CardRequest {
    /** The credit card number. */
    public String cardNumber;

    /** The billing address for this payment method. */
    public String billingAddress;

    /** The expiration date for this credit card. */
    public String expirationDate;

    /**
     * Checks if a {@code UpdateCardRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (cardNumber.isEmpty() || !expirationDate.contains("-")) {
            JsonResponse.send(exchange, 400, Map.of("error", "cardNumber and expirationDate (YYYY-MM) are required"));
            return false;
        } else {
            return true;
        } // if-else
    } // check
} // UpdateCardRequest