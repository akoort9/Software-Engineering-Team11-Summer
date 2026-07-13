package cs4050e.ces.db.theatre;

/**
 * Represents a showroom in a {@code Theatre} that hosts
 * {@code Movie}s at different {@code Showtime}s.
 */
public class Showroom {

    /** Database ID. */
    private int id = -1;

    /** Display name of the showroom, e.g. "Theatre 3". */
    private String name;

    /** Total number of seats in the showroom. */
    private int capacity;

    public Showroom(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    } // Showroom

    public int getId() {
        return id;
    } // getId

    public void setId(int id) {
        this.id = id;
    } // setId

    public String getName() {
        return name;
    } // getName

    public int getCapacity() {
        return capacity;
    } // getCapacity
} // Showroom
