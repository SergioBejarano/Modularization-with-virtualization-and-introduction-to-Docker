package co.edu.escuelaing.microspringboot.examples;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestGreetingController {

    @Test
    public void shouldReturnGreeting() {
        String result = GreetingController.greeting("Sergio");
        assertEquals("Hello Sergio", result);
    }

    @Test
    public void shouldReturnDefaultGreeting() {
        String result = GreetingController.greeting("World");
        assertEquals("Hello World", result);
    }
}
