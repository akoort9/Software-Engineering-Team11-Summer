package cs4050e.deliv2.db;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/** Provides methods to handle stored in a given database. */
public class DataHandler {
    /** GSON object. */
    private static final Gson GSON = new GsonBuilder()
	.create();

    /**
     * Adds listings to the provided database.
     * @param listings an array of {@code Listing}s.
     * @param filepath the path of the database.
     */
    public static boolean addListings(Listing[] listings, String filepath) {
	try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
	    for (int i = 0; i < listings.length; i++) {
		writer.write(GSON.toJson(listings[i]));
		writer.newLine();
	    } // for
	    writer.close();
	    return true;
	} catch (IOException ioe) {
	    System.err.println("addListings: " + ioe);
	    return false;
	} // try-catch
    } // addListings


    /**
     * Grabs the listings in the database and converts them
     * into an array of {@code Listing} objects.
     * @param filepath the filepath of the database
     * @return an array of {@code Listing}s if successful, {@code null} otherwise.
     */
    public static Listing[] getListings(String filepath) {
	String buf;
	Listing[] listings = new Listing[10];

	// read lines of db
	try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
	    // convert JSON to objects
	    for (int i = 0; i < 10; i++) {
		if ((buf = reader.readLine()) != null) {
		    System.out.println(GSON.fromJson(buf, Listing.class));
		    listings[i] = GSON.fromJson(buf, Listing.class);
		} // if
	    } // for

	    reader.close();
	    return listings;
	} catch (IOException ioe) {
	    System.err.println("getListings: " + ioe);
	    return null;
	} // try-catch
    } // getListings
} // LoadData
