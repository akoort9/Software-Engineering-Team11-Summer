package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a request for a user's card. */
public class CardRequest extends UserRequest {
    /** The database ID of the credit card. */
    public int cardId;

    /**
     * Checks if a {@code CardRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (cardId == -1) {
            JsonResponse.send(exchange, 400, Map.of("error", "cardId is required"));
            return false;
        } else {
            return true;
        } // if-else
    } // check
} // CardRequest
