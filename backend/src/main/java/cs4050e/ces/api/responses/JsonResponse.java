package cs4050e.ces.api.responses;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

/** Represents a response using JSON. */
public class JsonResponse implements Response {
    /** GSON object. */
    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Writes a JSON response with the given status code.
     * @param exchange The HTTP exchange to respond to.
     * @param status The HTTP status code.
     * @param payload The object to serialize as JSON.
     * @throws IOException if writing the response fails.
     */
    public static void send(HttpExchange exchange, int status, Object payload) throws IOException {
      byte[] bytes = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(status, bytes.length);

		  try (OutputStream os = exchange.getResponseBody()) {
			  os.write(bytes);
		  } // try
    } // send
} // JsonResponse
