package cs4050e.ces.db.theatre;

/** Represents a seat in a {@code Showroom}. */
public class Seat {

    /** Database ID. */
    private int id = -1;

    /** The showroom this seat belongs to. */
    private int showroomId;

    /** Row of the seat, e.g. "A". */
    private String rowLabel;

    /** Seat number within the row, e.g. 12 (displayed as "A12"). */
    private int seatNumber;

    public Seat(int showroomId, String rowLabel, int seatNumber) {
        this.showroomId = showroomId;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
    } // Seat

    public int getId() {
        return id;
    } // getId

    public void setId(int id) {
        this.id = id;
    } // setId

    public int getShowroomId() {
        return showroomId;
    } // getShowroomId

    public String getRowLabel() {
        return rowLabel;
    } // getRowLabel

    public int getSeatNumber() {
        return seatNumber;
    } // getSeatNumber

    /** @return the seat's display label, e.g. "A12". */
    public String getLabel() {
        return rowLabel + seatNumber;
    } // getLabel
} // Seat
