package co.edu.escuelaing.microspringboot.examples;

import java.util.ArrayList;
import java.util.List;

import co.edu.escuelaing.microspringboot.annotations.GetMapping;
import co.edu.escuelaing.microspringboot.annotations.RequestParam;
import co.edu.escuelaing.microspringboot.annotations.RestController;

/**
 *
 * @author sergio.bejarano-r
 */
@RestController
public class MathController {

    /**
     * Gets the square of a number.
     *
     * @param n the number to square
     * @return a string representation of the result
     */
    @GetMapping("/square")
    public static String square(@RequestParam(value = "n", defaultValue = "2") String n) {
        try {
            int num = Integer.parseInt(n);
            return "El cuadrado de " + num + " es " + (num * num);
        } catch (NumberFormatException e) {
            return "Error: el parámetro 'n' debe ser un número entero.";
        }
    }

    /**
     * Gets the factors of a number.
     *
     * @param n the number to factor
     * @return a string representation of the factors
     */
    @GetMapping("/factors")
    public static String factors(@RequestParam(value = "n", defaultValue = "1") String n) {
        try {
            int num = Integer.parseInt(n);
            if (num <= 1) {
                return "El número debe ser mayor que 1.";
            }

            List<Integer> factors = new ArrayList<>();
            int divisor = 2;
            int temp = num;

            while (temp > 1) {
                while (temp % divisor == 0) {
                    factors.add(divisor);
                    temp /= divisor;
                }
                divisor++;
            }

            return "Factores primos de " + num + " = " + factors.toString();

        } catch (NumberFormatException e) {
            return "Error: el parámetro 'n' debe ser un número entero.";
        }
    }

}
