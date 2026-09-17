package kz.iitu.springlab.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Value("${app.owner:unknown}")
    private String owner;

    @GetMapping("/hello")
    public Greeting hello(@RequestParam(defaultValue = "world") String name) {
        return new Greeting("Hello, " + name + "!", owner, LocalDateTime.now());
    }

    @GetMapping("/info")
    public Info info() {
        return new Info(owner,
                System.getProperty("java.version"),
                Runtime.getRuntime().availableProcessors());
    }
    // Individual assignment (variant 5): Fahrenheit -> Celsius and Kelvin conversion
    @GetMapping("/temperature")
    public TemperatureResult temperature(@RequestParam(required = false) Double f) {
        if (f == null) {
            throw new IllegalArgumentException("Parameter 'f' (Fahrenheit) is required");
        }
        double celsius = (f - 32) * 5.0 / 9.0;
        double kelvin = celsius + 273.15;
        return new TemperatureResult(f, round2(celsius), round2(kelvin));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex) {
        return ex.getMessage();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public record Greeting(String message, String owner, LocalDateTime timestamp) { }

    public record Info(String owner, String javaVersion, int cpuCores) { }

    public record TemperatureResult(double fahrenheit, double celsius, double kelvin) { }
}