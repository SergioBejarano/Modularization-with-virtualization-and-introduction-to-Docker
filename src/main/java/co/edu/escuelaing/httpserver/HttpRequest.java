package co.edu.escuelaing.httpserver;

import java.net.URI;

/**
 *
 * @author sergio.bejarano-r
 */
public class HttpRequest {

    URI requri = null;

    HttpRequest(URI requri) {
        this.requri = requri;
    }

    /**
     * Gets the value of a query parameter from the request URI.
     *
     * @param paramName the name of the query parameter
     * @return the value of the query parameter, or null if not found
     */
    public String getValue(String paramName) {

        String paramValue = requri.getQuery().split("=")[1];
        return paramValue;
    }

}
