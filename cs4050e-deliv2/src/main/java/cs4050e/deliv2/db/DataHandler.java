package cs4050e.deliv2.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.Charset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/** Provides methods to handle stored in a given database. */
public class DataHandler {
    /** GSON object. */
    private static final Gson GSON = new GsonBuilder()
	.setPrettyPrinting()
	.create();

    /** Listings to be added to the database. */
    private Listing[] listings;

    private Charset charset = Charset.forName("US-ASCII");
    
    /**
     * Adds listings to the provided database.
     * @param listings an array of {@code Listing}s.
     * @param filepath the path of the database.
     */
    public boolean addListings(Listing[] listings, String filepath) {
	throw new UnsupportedOperationException("method not yet implemented.");
    } // addListings
    
} // LoadData
