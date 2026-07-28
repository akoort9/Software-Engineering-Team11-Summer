package cs4050e.ces.api.requests;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a request to schedule one or more repeating showtimes for a
 * movie, defined by a start time, a length, and a number of days to repeat.
 */
public class ScheduleShowtimesRequest extends MovieRequest {
    /** Parser for the start time, matching the HTML datetime-local format. */
    private transient DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /** The database ID of the showtimes' showroom. */
    public int showroomID;

    /** The first showtime's start, formatted "yyyy-MM-dd'T'HH:mm". */
    public String startTime;

    /** The length of the movie in minutes. */
    public int durationMinutes;

    /** How many consecutive days to repeat the showtime; at least 1. */
    public int repeatDays;

    /**
     * Checks if a {@code ScheduleShowtimesRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @Override
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (!db.showroomExists(showroomID)) {
            JsonResponse.send(exchange, 404, Map.of("error", "invalid showroom id"));
            return false;
        } else if (durationMinutes <= 0) {
            JsonResponse.send(exchange, 400, Map.of("error", "duration must be greater than zero"));
            return false;
        } else if (repeatDays < 1) {
            JsonResponse.send(exchange, 400, Map.of("error", "repeat days must be at least one"));
            return false;
        } else if (startTime == null || startTime.isEmpty()) {
            JsonResponse.send(exchange, 400, Map.of("error", "invalid format"));
            return false;
        } else {
            try {
                getStartTime();
                return true;
            } catch (DateTimeParseException dtpe) {
                JsonResponse.send(exchange, 400, Map.of("error", "invalid format"));
                return false;
            } // try-catch
        } // if-else
    } // check

    /**
     * Parses the start time string into a {@code LocalDateTime}.
     * @return The first showtime's start.
     * @throws DateTimeParseException if the string is invalid.
     */
    public LocalDateTime getStartTime() {
        return LocalDateTime.parse(startTime, formatter);
    } // getStartTime
} // ScheduleShowtimesRequest
