package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;
import cs4050e.ces.db.DataHandler;

/** Represents a request that requires administrator access. */
public class AdminRequest implements Request {
    /** Database access point. */
    protected static final DataHandler db = DataHandler.getInstance();

    /** The admin submitting this request. */
    protected String adminEmail;

    /**
     * Checks that the request comes from a known administrator.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!db.userExists(adminEmail)) {
            JsonResponse.send(exchange, 404, Map.of("error", "user not found"));
            return false;
        } else if (!db.getUser(adminEmail).isAdmin()) {
            JsonResponse.send(exchange, 403, Map.of("error", "requires administrator access"));
            return false;
        } else {
            return true;
        } // if-else
    } // check
} // AdminRequest
