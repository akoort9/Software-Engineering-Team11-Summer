package cs4050e.ces.db.theatre;

import java.sql.Timestamp;

/**
 * Represents a showtime in a given {@code Showroom}
 * for a given {@code Movie}.
 */
public class Showtime {

    /** Database ID. */
    private int id = -1;

    /** The movie playing at this showtime. */
    private int movieId;

    /** The showroom hosting this showtime. */
    private int showroomId;

    /** When the showing starts. */
    private Timestamp startTime;

    /** When the showing ends. */
    private Timestamp endTime;

    public Showtime(int movieId, int showroomId, Timestamp startTime, Timestamp endTime) {
        this.movieId = movieId;
        this.showroomId = showroomId;
        this.startTime = startTime;
        this.endTime = endTime;
    } // Showtime

    public int getId() {
        return id;
    } // getId

    public void setId(int id) {
        this.id = id;
    } // setId

    public int getMovieId() {
        return movieId;
    } // getMovieId

    public int getShowroomId() {
        return showroomId;
    } // getShowroomId

    public Timestamp getStartTime() {
        return startTime;
    } // getStartTime

    public Timestamp getEndTime() {
        return endTime;
    } // getEndTime
} // Showtime
