package co.edu.escuelaing.microspringboot;

import co.edu.escuelaing.httpserver.HttpServer;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 * @author sergio.bejarano-r
 */
public class MicroSpringBoot {

    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println("Starting MicroSpringBoot");
        int port = getPort();
        HttpServer.runServer(port);
    }

    private static int getPort() {
        String portEnv = System.getenv("PORT");
        if (portEnv != null) {
            try {
                return Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT environment variable, using default 9000");
            }
        }
        return 9000;
    }
}
