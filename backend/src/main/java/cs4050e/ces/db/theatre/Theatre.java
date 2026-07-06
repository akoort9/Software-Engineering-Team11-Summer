package cs4050e.ces.db.theatre;

import java.util.List;
import java.util.ArrayList;

/** Represents a theatre where this system is active */
public class Theatre {

    /** Database ID */
    public int theatreId;

    /** Theatre name.  */
    public String name;

    /** Address of the theatre. */
    public String address;

    /** List of showrooms available in this theatre. */
    public List<Showroom> showrooms;

    /**
     * Initializes a new {@code Theatre} object.
     * @param id The database ID of this object.
     * @param name The name of the theatre.
     * @param address The address of the theatre.
     * @param showroom A showroom for the theatre.
     * @throws IllegalArgumentException if any {@code Object} arguments are null.
     */
    public Theatre(int id, String name, String address, Showroom showroom) {
        if (name == null || address == null || showroom == null) {
            throw new IllegalArgumentException("constructor cannot have null arguments.");
        } // if

        this.theatreId = id;
        this.name = name;
        this.address = address;
        this.showrooms = new ArrayList<Showroom>();
        this.showrooms.add(showroom);
    } // Theatre

    /**
     * Initializes a new {@code Theatre} object.
     * @param id The database ID of this object.
     * @param name The name of the theatre.
     * @param address The address of the theatre.
     * @param showrooms The showrooms for the theatre.
     * @throws IllegalArgumentException if any {@code Object} arguments are null.
     */
    public Theatre(int id, String name, String address, List<Showroom> showrooms) {
        if (name == null || address == null || showrooms == null) {
            throw new IllegalArgumentException("constructor cannot have null arguments.");
        } // if

        this.theatreId = id;
        this.name = name;
        this.address = address;
        this.showrooms = new ArrayList<Showroom>();

        // populate showroom list
        for (int i = 0; i < showrooms.size(); i++) {
            if (showrooms.get(i) != null) {
                this.showrooms.add(showrooms.get(i));
            } // if
        } // for
     } // Theatre
} // Theatre
