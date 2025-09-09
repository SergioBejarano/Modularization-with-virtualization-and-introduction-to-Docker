package co.edu.escuelaing.microspringboot.examples;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestMathController {

    @Test
    public void shouldReturnSquareOfNumber() {
        String result = MathController.square("4");
        assertTrue(result.contains("El cuadrado de 4 es 16"));
    }

    @Test
    public void shouldReturnErrorForNonIntegerSquare() {
        String result = MathController.square("abc");
        assertTrue(result.contains("Error"));
    }

    @Test
    public void shouldReturnFactorsOfNumber() {
        String result = MathController.factors("12");
        assertTrue(result.contains("Factores primos de 12 = [2, 2, 3]"));
    }

    @Test
    public void shouldReturnErrorForNonIntegerFactors() {
        String result = MathController.factors("xyz");
        assertTrue(result.contains("Error"));
    }

    @Test
    public void shouldReturnErrorForFactorsLessThanOrEqualToOne() {
        String result = MathController.factors("1");
        assertTrue(result.contains("El número debe ser mayor que 1."));
    }
}
