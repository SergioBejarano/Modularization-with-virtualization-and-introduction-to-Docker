package co.edu.escuelaing.httpserver;

import co.edu.escuelaing.microspringboot.annotations.GetMapping;
import co.edu.escuelaing.microspringboot.annotations.RequestParam;
import co.edu.escuelaing.microspringboot.annotations.RestController;
import java.net.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author sergio.bejarano-r
 */
public class HttpServer {

    public static Map<String, Method> services = new HashMap();

    /**
     * Loads services annotated with @RestController and @GetMapping.
     */
    public static void loadServices() {
        try {
            String baseDir = "co/edu/escuelaing/microspringboot/examples";
            File dir = new File(HttpServer.class.getClassLoader().getResource(baseDir).toURI());
            File[] files = dir.listFiles((d, name) -> name.endsWith(".class"));
            if (files != null) {
                for (File file : files) {
                    String className = "co.edu.escuelaing.microspringboot.examples."
                            + file.getName().replace(".class", "");
                    Class<?> c = Class.forName(className);
                    if (c.isAnnotationPresent(RestController.class)) {
                        Method[] methods = c.getDeclaredMethods();
                        for (Method m : methods) {
                            if (m.isAnnotationPresent(GetMapping.class)) {
                                String mapping = m.getAnnotation(GetMapping.class).value();
                                services.put(mapping, m);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.getLogger(HttpServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    /**
     * Starts the HTTP server.
     */
    private static volatile boolean running = true;

    public static void runServer(int port) throws IOException, URISyntaxException {
        loadServices();

        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            return;
        }

        // Thread pool for handling clients
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        // Shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutdown hook triggered. Stopping server...");
            running = false;
            try {
                serverSocket.close();
            } catch (IOException e) {
                // Ignore
            }
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
            System.out.println("Server stopped gracefully.");
        }));

        System.out.println("Server started on port " + port + ". Press Ctrl+C to stop.");

        while (running) {
            try {
                final Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Accept failed: " + e.getMessage());
                }
            }
        }

        // Cleanup if not already done by shutdown hook
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                InputStream inStream = clientSocket.getInputStream();
                OutputStream rawOut = clientSocket.getOutputStream();
                PrintWriter out = new PrintWriter(rawOut, true);
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));) {
            String inputLine;
            boolean firstline = true;
            URI requri = null;

            while ((inputLine = in.readLine()) != null) {
                if (firstline) {
                    requri = new URI(inputLine.split(" ")[1]);
                    System.out.println("Path: " + requri.getPath());
                    firstline = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            if (requri != null && requri.getPath().startsWith("/app")) {
                String response = invokeService(requri);
                out.println(response);
            } else if (requri != null) {
                serveStaticFile(requri.getPath(), rawOut);
            }
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Invokes the appropriate service method based on the request URI.
     *
     * @param requri the request URI
     * @return the HTTP response as a string
     */
    private static String invokeService(URI requri) {
        String header = "HTTP/1.1 200 OK\n\r"
                + "content-type: text/html\n\r"
                + "\n\r";
        try {
            HttpRequest req = new HttpRequest(requri);
            HttpResponse res = new HttpResponse();
            String servicePath = requri.getPath().substring(4);
            Method m = services.get(servicePath);
            String[] argValues = null;
            RequestParam rp = (RequestParam) m.getParameterAnnotations()[0][0];
            if (requri.getQuery() == null) {
                argValues = new String[] { rp.defaultValue() };
            } else {
                String queryParamName = rp.value();
                argValues = new String[] { req.getValue(queryParamName) };
            }
            return header + m.invoke(null, argValues);

        } catch (IllegalAccessException ex) {
            System.getLogger(HttpServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (InvocationTargetException ex) {
            System.getLogger(HttpServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return header + "Error!";
    }

    /**
     * Serves a static file to the client.
     *
     * @param path   the file path
     * @param rawOut the output stream to write the response
     */
    private static void serveStaticFile(String path, OutputStream rawOut) throws IOException {
        if (path.equals("/")) {
            path = "/index.html";
        }

        try {
            URL fileURL = HttpServer.class.getClassLoader().getResource("webroot" + path);
            if (fileURL == null) {
                send404(rawOut);
                return;
            }

            File file = new File(fileURL.toURI());
            if (!file.exists() || file.isDirectory()) {
                send404(rawOut);
                return;
            }

            String mimeType = guessContentType(path);
            byte[] data = readFileBytes(file);

            String header = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: " + mimeType + "\r\n"
                    + "Content-Length: " + data.length + "\r\n"
                    + "\r\n";

            rawOut.write(header.getBytes());
            rawOut.write(data);
            rawOut.flush();

        } catch (Exception e) {
            send404(rawOut);
        }
    }

    /**
     * Sends a 404 Not Found response.
     *
     * @param rawOut the output stream to write the response
     */
    private static void send404(OutputStream rawOut) throws IOException {
        String notFound = "HTTP/1.1 404 Not Found\r\n"
                + "Content-Type: text/html\r\n\r\n"
                + "<h1>404 Not Found</h1>";
        rawOut.write(notFound.getBytes());
        rawOut.flush();
    }

    /**
     * Reads the bytes of a file.
     *
     * @param file the file to read
     * @return the file contents as a byte array
     */
    private static byte[] readFileBytes(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = fis.readAllBytes();
        fis.close();
        return data;
    }

    /**
     * Guesses the content type based on the file extension.
     *
     * @param path the file path
     * @return the guessed content type
     */
    private static String guessContentType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm")) {
            return "text/html";
        } else if (path.endsWith(".css")) {
            return "text/css";
        } else if (path.endsWith(".js")) {
            return "application/javascript";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith(".gif")) {
            return "image/gif";
        } else if (path.endsWith(".ico")) {
            return "image/x-icon";
        }
        return "application/octet-stream";
    }

    /**
     * Starts the HTTP server.
     *
     * @param args command line arguments
     */
    public static void start(String[] args) throws IOException, URISyntaxException {
        int port = 9000;
        String portEnv = System.getenv("PORT");
        if (portEnv != null) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT environment variable, using default 9000");
            }
        }
        runServer(port);
    }

    public static String defaultResponse() {
        return "HTTP/1.1 200 OK\r\n"
                + "content-type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<title>Form Example</title>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "</head>\n"
                + "<body>\n"
                + "<h1>Form with GET</h1>\n"
                + "<form action=\"/hello\">\n"
                + "<label for=\"name\">Name:</label><br>\n"
                + "<input type=\"text\" id=\"name\" name=\"name\" value=\"John\"><br><br>\n"
                + "<input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n"
                + "</form>\n"
                + "<div id=\"getrespmsg\"></div>\n"
                + " \n"
                + "<script>\n"
                + "function loadGetMsg() {\n"
                + "let nameVar = document.getElementById(\"name\").value;\n"
                + "const xhttp = new XMLHttpRequest();\n"
                + "xhttp.onload = function() {\n"
                + "document.getElementById(\"getrespmsg\").innerHTML =\n"
                + "this.responseText;\n"
                + "}\n"
                + "xhttp.open(\"GET\", \"/app/hello?name=\"+nameVar);\n"
                + "xhttp.send();\n"
                + "}\n"
                + "</script>\n"
                + " \n"
                + "<h1>Form with POST</h1>\n"
                + "<form action=\"/hellopost\">\n"
                + "<label for=\"postname\">Name:</label><br>\n"
                + "<input type=\"text\" id=\"postname\" name=\"name\" value=\"John\"><br><br>\n"
                + "<input type=\"button\" value=\"Submit\" onclick=\"loadPostMsg(postname)\">\n"
                + "</form>\n"
                + " \n"
                + "<div id=\"postrespmsg\"></div>\n"
                + " \n"
                + "<script>\n"
                + "function loadPostMsg(name){\n"
                + "let url = \"/hellopost?name=\" + name.value;\n"
                + " \n"
                + "fetch (url, {method: 'POST'})\n"
                + ".then(x => x.text())\n"
                + ".then(y => document.getElementById(\"postrespmsg\").innerHTML = y);\n"
                + "}\n"
                + "</script>\n"
                + "</body>\n"
                + "</html>";
    }

}
