package org.weather.print;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.weather.model.WeatherResponse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TablePrinterTest {

    private static final Gson GSON = new Gson();

    private static final String TODAY_PLACEHOLDER_DAY =
            "{ \"mintemp_c\": 0.0, \"maxtemp_c\": 0.0, \"avghumidity\": 0, \"maxwind_kph\": 0.0 }";

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream captured;

    @BeforeEach
    void redirectStdOut() {
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStdOut() {
        System.setOut(originalOut);
    }

    private static String day(double minTemp, double maxTemp, int humidity, double windKph) {
        return "{ \"mintemp_c\": %s, \"maxtemp_c\": %s, \"avghumidity\": %d, \"maxwind_kph\": %s }"
                .formatted(minTemp, maxTemp, humidity, windKph);
    }

    /**
     * Builds a response with today (placeholder) and tomorrow's day/hours — tomorrow is what
     * {@link org.weather.properties.WeatherParameterRegistry} reads.
     */
    private WeatherResponse twoDayResponse(String tomorrowDayJson, String tomorrowHoursJson) {
        String json = """
                { "forecast": { "forecastday": [
                    { "date": "2026-09-21", "day": %s },
                    { "date": "2026-09-22", "day": %s%s }
                ] } }
                """.formatted(TODAY_PLACEHOLDER_DAY, tomorrowDayJson, tomorrowHoursJson);
        return GSON.fromJson(json, WeatherResponse.class);
    }

    private WeatherResponse response(double minTemp, double maxTemp) {
        return twoDayResponse(day(minTemp, maxTemp, 50, 10.0), "");
    }

    private WeatherResponse responseWithoutHours() {
        return twoDayResponse(day(5.0, 15.0, 50, 10.0), "");
    }

    private WeatherResponse singleDayResponse() {
        String json = """
                { "forecast": { "forecastday": [
                    { "date": "2026-09-21", "day": %s }
                ] } }
                """.formatted(day(5.0, 15.0, 40, 10.0));
        return GSON.fromJson(json, WeatherResponse.class);
    }

    private String output() {
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    void printsHeaderWithCityColumnFirst() {
        Map<String, WeatherResponse> results = new LinkedHashMap<>();
        results.put("Kyiv", response(5.0, 15.0));

        TablePrinter.print(List.of("minTemp", "maxTemp"), results);

        String[] lines = output().lines().toArray(String[]::new);
        assertEquals("| City | minTemp | maxTemp |", lines[1]);
    }

    @Test
    void printsOneRowPerCityInInsertionOrder() {
        Map<String, WeatherResponse> results = new LinkedHashMap<>();
        results.put("Kyiv", response(5.0, 15.0));
        results.put("Madrid", response(12.0, 25.0));

        TablePrinter.print(List.of("minTemp", "maxTemp"), results);

        String out = output();
        assertTrue(out.contains("| Kyiv   | 5.0     | 15.0    |"));
        assertTrue(out.contains("| Madrid | 12.0    | 25.0    |"));
    }

    @Test
    void printsIntegerParameterAlongsideDoubleParameter() {
        Map<String, WeatherResponse> results = new LinkedHashMap<>();
        results.put("Kyiv", response(5.0, 15.0));

        TablePrinter.print(List.of("minTemp", "humidity"), results);

        // humidity is an int extractor (unlike minTemp/maxTemp) — must render as "50", not "50.0".
        assertTrue(output().contains("| Kyiv | 5.0     | 50       |"));
    }

    @Test
    void printsNaWhenExtractorReturnsNull() {
        Map<String, WeatherResponse> results = new LinkedHashMap<>();
        results.put("Kyiv", responseWithoutHours());

        TablePrinter.print(List.of("windDirection"), results);

        // no hourly data for tomorrow -> extractWindDirection() is empty -> orElse(null) -> "n/a".
        assertTrue(output().contains("| Kyiv | n/a           |"));
    }

    @Test
    void printsOnlyCityColumnWhenParametersEmpty() {
        Map<String, WeatherResponse> results = new LinkedHashMap<>();
        results.put("Kyiv", response(5.0, 15.0));

        TablePrinter.print(List.of(), results);

        String[] lines = output().lines().toArray(String[]::new);
        assertEquals("| City |", lines[1]);
        assertEquals("| Kyiv |", lines[3]);
    }

    @Test
    void printsErrForOneCityWhileOthersSucceed() {
        Map<String, WeatherResponse> results = new LinkedHashMap<>();
        results.put("Kyiv", response(5.0, 15.0));
        results.put("Odesa", singleDayResponse()); // no forecast for tomorrow -> extractor throws
        results.put("Madrid", response(12.0, 25.0));

        TablePrinter.print(List.of("minTemp"), results);

        // one broken city must not stop the others from being printed — we report what we can.
        String out = output();
        assertTrue(out.contains("| Kyiv   | 5.0     |"));
        assertTrue(out.contains("| Odesa  | err     |"));
        assertTrue(out.contains("| Madrid | 12.0    |"));
    }

    @Test
    void printsNothingButHeaderWhenNoResults() {
        TablePrinter.print(List.of("minTemp"), Map.of());

        long dataRows = output().lines()
                .filter(line -> line.startsWith("|") && !line.contains("City"))
                .count();
        assertEquals(0, dataRows);
    }

    @Test
    void printsErrWhenExtractorThrows() {
        // The extractor itself throwing (e.g. no forecast for tomorrow) is covered directly in
        // WeatherParameterRegistryTest; this test only checks TablePrinter's own contract: it
        // catches any extractor failure and renders "err" instead of propagating.
        Map<String, WeatherResponse> results = new LinkedHashMap<>();
        results.put("Kyiv", singleDayResponse());

        TablePrinter.print(List.of("minTemp"), results);

        assertTrue(output().contains("| Kyiv | err     |"));
    }
}
