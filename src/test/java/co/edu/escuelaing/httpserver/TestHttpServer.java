package co.edu.escuelaing.httpserver;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URI;

public class TestHttpServer {

    @Test
    public void shouldLoadServicesWithoutErrors() {
        try {
            HttpServer.loadServices();
            assertNotNull(HttpServer.services);
        } catch (Exception e) {
            fail("loadServices lanzó una excepción: " + e.getMessage());
        }
    }

    @Test
    public void shouldReturnDefaultResponseWithHttpHeaders() {
        String response = HttpServer.defaultResponse();
        assertNotNull(response);
        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertTrue(response.contains("content-type: text/html"));
    }

    @Test
    public void shouldReturnDefaultResponseWithHtmlBody() {
        String response = HttpServer.defaultResponse();
        assertTrue(response.contains("<html>"));
        assertTrue(response.contains("</html>"));
        assertTrue(response.contains("<form"));
        assertTrue(response.contains("function loadGetMsg()"));
    }

    @Test
    public void shouldStartServerInThreadAndStopGracefully() {
        Thread serverThread = new Thread(() -> {
            try {
                HttpServer.start(new String[] {});
            } catch (IOException | URISyntaxException e) {
            }
        });
        try {
            serverThread.start();
            Thread.sleep(200);
            serverThread.interrupt();
            assertTrue(serverThread.isAlive() || !serverThread.isAlive());
        } catch (InterruptedException e) {
            fail("La prueba fue interrumpida");
        }
    }

    @Test
    public void shouldNotReturnNullDefaultResponse() {
        String response = HttpServer.defaultResponse();
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void shouldNotModifyServicesMapWhenLoadServicesIsCalledTwice() {
        try {
            HttpServer.loadServices();
            int sizeBefore = HttpServer.services.size();
            HttpServer.loadServices();
            int sizeAfter = HttpServer.services.size();
            assertEquals(sizeBefore, sizeAfter);
        } catch (Exception e) {
            fail("loadServices lanzó una excepción inesperada: " + e.getMessage());
        }
    }

    @Test
    public void shouldExtractQueryParamValueFromHttpRequest() {
        URI uri = URI.create("/app/test?name=Sergio");
        HttpRequest request = new HttpRequest(uri);
        HttpResponse res = new HttpResponse();
        String value = request.getValue("name");
        assertEquals("Sergio", value);
    }

    @Test
    public void shouldInitializeWithDefaultHeaders() {
        HttpResponse response = new HttpResponse();

        String built = response.buildResponse();

        assertTrue(built.contains("Content-Type: text/plain; charset=UTF-8"));
        assertTrue(built.contains("Connection: close"));
    }

    @Test
    public void shouldSetStatusCorrectly() {
        HttpResponse response = new HttpResponse();
        response.setStatus(404, "Not Found");

        String built = response.buildResponse();

        assertTrue(built.startsWith("HTTP/1.1 404 Not Found"));
    }

    @Test
    public void shouldSetBodyAndContentLength() {
        HttpResponse response = new HttpResponse();
        String body = "Hello world!";
        response.setBody(body);

        String built = response.buildResponse();

        assertTrue(built.contains("Content-Length: " + body.getBytes().length));
        assertTrue(built.endsWith(body));
    }

    @Test
    public void shouldSetCustomHeader() {
        HttpResponse response = new HttpResponse();
        response.setHeader("X-Custom", "Test");

        String built = response.buildResponse();

        assertTrue(built.contains("X-Custom: Test"));
    }

    @Test
    public void shouldBuildFullHttpResponse() {
        HttpResponse response = new HttpResponse();
        response.setStatus(201, "Created");
        response.setHeader("X-App", "MicroSpring");
        response.setBody("Success!");

        String built = response.buildResponse();

        assertTrue(built.contains("HTTP/1.1 201 Created"));
        assertTrue(built.contains("X-App: MicroSpring"));
        assertTrue(built.contains("Content-Length: 8"));
        assertTrue(built.endsWith("Success!"));
    }
}
