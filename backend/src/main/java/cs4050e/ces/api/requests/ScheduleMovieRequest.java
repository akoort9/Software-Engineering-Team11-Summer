package cs4050e.ces.api.requests;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;

/** Represents a request to add a showtime for a movie. */
public class ScheduleMovieRequest extends MovieRequest {
    /** Date formatter. */
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    /** The database ID of this showtime's showroom. */
    public int showroomID;

    /** 
     * Format: "yyyy-MM-dd'T'HH:mm"
     * yyyy - Year
     * MM - Month
     * dd - Day
     * HH - Hour, on 24-hr clock
     * mm - Minutes
     * Ex: "2026-08-06'T'20:00" -> August 6th, 2026 at 8:00 PM
     * */
    private String startTime;
    private String endTime;

    /**
     * Checks if a {@code ScheduleMovieRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (!db.showroomExists(showroomID)) {
            JsonResponse.send(exchange, 404, Map.of("error", "invalid showroom id"));
            return false;
        } else if (startTime.isEmpty() || endTime.isEmpty()) {
            JsonResponse.send(exchange, 400, Map.of("error", "invalid format"));
            return false;
        } else {
            try {
                getStartTime();
                getEndTime();
                return true;
            } catch (ParseException pe) {
                JsonResponse.send(exchange, 400, Map.of("error", "invalid format"));
                return false;
            } // try-catch
        } // if-else
    } // check

    /**
     * Parses start time string and returns a {@code Date}.
     * @return The start time.
     * @throws ParseException if the string is invalid.
     */
    public Timestamp getStartTime() throws ParseException {
        // convert java.util.Date to java.sql.Timestamp, preserving time-of-day
        return new Timestamp(formatter.parse(startTime).getTime());
    } // getStartTime

    /**
     * Parses end time string and returns a {@code Date}.
     * @return The end time.
     * @throws ParseException if the string is invalid.
     */
    public Timestamp getEndTime() throws ParseException {
        // convert java.util.Date to java.sql.Timestamp, preserving time-of-day
        return new Timestamp(formatter.parse(endTime).getTime());
    } // getEndTime
} // ScheduleMovieRequest
