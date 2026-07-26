package cs4050e.ces.api.requests;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;

import cs4050e.ces.api.responses.JsonResponse;
import cs4050e.ces.db.payment.Ticket;
import cs4050e.ces.db.theatre.Seat;
import cs4050e.ces.db.theatre.Showtime;

/** Represents a request to book one or more seats for a showtime. */
public class BookTicketsRequest extends UserRequest {

    /** One seat and the ticket class it should be booked as. */
    public static class SeatSelection {
        /** The database id of the seat to book. */
        public int seatId = -1;

        /** The ticket class for this seat, e.g. "standard", "child", "senior". */
        public String ticketType;
    } // SeatSelection

    /** The database id of the showtime being booked. */
    public int showtimeId = -1;

    /** The seats being booked, each with its own ticket class. */
    public List<SeatSelection> seats;

    /**
     * Checks if a {@code BookTicketsRequest} is valid.
     * @param exchange The exchange to send error codes to.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean check(HttpExchange exchange) throws IOException {
        if (!super.check(exchange)) {
            return false;
        } else if (showtimeId == -1) {
            JsonResponse.send(exchange, 400, Map.of("error", "showtimeId is required"));
            return false;
        } // if-else

        Showtime showtime = db.getShowtime(showtimeId);
        if (showtime == null) {
            JsonResponse.send(exchange, 404, Map.of("error", "showtime not found"));
            return false;
        } else if (seats == null || seats.isEmpty()) {
            JsonResponse.send(exchange, 400, Map.of("error", "at least one seat is required"));
            return false;
        } // if-else

        List<Seat> roomSeats = db.getSeats(showtime.getShowroomId());
        Set<Integer> validSeatIds = new HashSet<Integer>();
        for (Seat seat : roomSeats) {
            validSeatIds.add(seat.getId());
        } // for

        for (SeatSelection selection : seats) {
            if (!validSeatIds.contains(selection.seatId)) {
                JsonResponse.send(exchange, 400, Map.of("error", "invalid seatId: " + selection.seatId));
                return false;
            } // if

            try {
                Ticket.TicketType.valueOf(selection.ticketType.toUpperCase());
            } catch (Exception e) {
                JsonResponse.send(exchange, 400, Map.of("error", "invalid ticket type: " + selection.ticketType));
                return false;
            } // try-catch
        } // for

        return true;
    } // check
} // BookTicketsRequest
