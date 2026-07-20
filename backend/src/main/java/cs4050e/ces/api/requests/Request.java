package cs4050e.ces.api.requests;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

/** Represents a request from the frontend. */
public interface Request {
    public boolean check(HttpExchange exchange) throws IOException;
} // Request
