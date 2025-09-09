package co.edu.escuelaing.httpserver;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP response that will be sent back to the client.
 * Contains status, headers, and body.
 * 
 * @author sergio.bejarano-r
 */
public class HttpResponse {

    private int statusCode = 200;
    private String statusMessage = "OK";
    private String body = "";
    private final Map<String, String> headers = new HashMap<>();

    /*
     * Initializes default headers.
     */
    public HttpResponse() {
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        headers.put("Connection", "close");
    }

    /**
     * Sets the HTTP status code and message.
     *
     * @param code    HTTP status code
     * @param message HTTP status message
     */
    public void setStatus(int code, String message) {
        this.statusCode = code;
        this.statusMessage = message;
    }

    /**
     * Sets the body of the response.
     *
     * @param body response body
     */
    public void setBody(String body) {
        this.body = body;
        headers.put("Content-Length", String.valueOf(body.getBytes().length));
    }

    /**
     * Sets a header in the response.
     *
     * @param key   header name
     * @param value header value
     */
    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * Builds the full HTTP response as a String.
     */
    public String buildResponse() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");
        headers.forEach((k, v) -> response.append(k).append(": ").append(v).append("\r\n"));
        response.append("\r\n");
        response.append(body);
        return response.toString();
    }
}
