package cs4050e.ces.api.requests;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents an admin request to create a promotion. */
public class PromotionRequest extends AdminRequest {
    /** The code customers enter to activate the promotion. */
    public String promoCode;

    /** The percentage taken off the price (e.g. 10 for 10% off). */
    public double percentOff;

    /** The date the promotion expires, formatted yyyy-MM-dd. */
    public String expirationDate;

    /**
     * Checks that the promotion details are valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @Override
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (promoCode == null || promoCode.trim().isEmpty()) {
            JsonResponse.send(exchange, 400, Map.of("error", "promo code is required"));
            return false;
        } else if (percentOff <= 0 || percentOff >= 100) {
            JsonResponse.send(exchange, 400, Map.of("error", "discount must be between 0 and 100"));
            return false;
        } // if-else

        try {
            LocalDate.parse(expirationDate);
            return true;
        } catch (DateTimeParseException | NullPointerException e) {
            JsonResponse.send(exchange, 400, Map.of("error", "invalid expiration date"));
            return false;
        } // try-catch
    } // check

    /**
     * Returns the parsed expiration date.
     * @return The expiration date.
     */
    public LocalDate getExpirationDate() {
        return LocalDate.parse(expirationDate);
    } // getExpirationDate

    /**
     * Returns the discount as a price multiplier (e.g. 0.9 for 10% off).
     * @return The discount multiplier.
     */
    public double getDiscountMultiplier() {
        return 1.0 - (percentOff / 100.0);
    } // getDiscountMultiplier
} // PromotionRequest
