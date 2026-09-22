package org.weather.properties;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.weather.model.WeatherResponse;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class WeatherParameterRegistryTest {

    private static final Gson GSON = new Gson();

    private WeatherResponse response(String json) {
        return GSON.fromJson(json, WeatherResponse.class);
    }

    private static final String TWO_DAYS_WITH_HOURS = """
            {
              "forecast": {
                "forecastday": [
                  { "date": "2026-09-21", "day": { "mintemp_c": 5.0, "maxtemp_c": 15.0, "avghumidity": 40, "maxwind_kph": 10.0 } },
                  {
                    "date": "2026-09-22",
                    "day": { "mintemp_c": 10.5, "maxtemp_c": 20.5, "avghumidity": 55, "maxwind_kph": 18.0 },
                    "hour": [
                      { "wind_dir": "NW", "wind_degree": 315 },
                      { "wind_dir": "NW", "wind_degree": 320 },
                      { "wind_dir": "N", "wind_degree": 350 }
                    ]
                  }
                ]
              }
            }
            """;

    @Test
    void supportedKeysContainsAllRegisteredParameters() {
        assertEquals(
                Set.of("minTemp", "maxTemp", "humidity", "windSpeed", "windDirection"),
                WeatherParameterRegistry.supportedKeys());
    }

    @Test
    void extractorForUnknownKeyIsEmpty() {
        assertTrue(WeatherParameterRegistry.extractorFor("unknown").isEmpty());
    }

    @Test
    void extractsMinTempForTomorrow() {
        WeatherResponse r = response(TWO_DAYS_WITH_HOURS);
        Function<WeatherResponse, Object> extractor = WeatherParameterRegistry.extractorFor("minTemp").orElseThrow();
        assertEquals(10.5, extractor.apply(r));
    }

    @Test
    void extractsMaxTempForTomorrow() {
        WeatherResponse r = response(TWO_DAYS_WITH_HOURS);
        Function<WeatherResponse, Object> extractor = WeatherParameterRegistry.extractorFor("maxTemp").orElseThrow();
        assertEquals(20.5, extractor.apply(r));
    }

    @Test
    void extractsHumidityForTomorrow() {
        WeatherResponse r = response(TWO_DAYS_WITH_HOURS);
        Function<WeatherResponse, Object> extractor = WeatherParameterRegistry.extractorFor("humidity").orElseThrow();
        assertEquals(55, extractor.apply(r));
    }

    @Test
    void extractsWindSpeedForTomorrow() {
        WeatherResponse r = response(TWO_DAYS_WITH_HOURS);
        Function<WeatherResponse, Object> extractor = WeatherParameterRegistry.extractorFor("windSpeed").orElseThrow();
        assertEquals(18.0, extractor.apply(r));
    }

    @Test
    void dayBasedExtractorThrowsWhenTomorrowIsMissing() {
        // TablePrinter is what turns this into a friendly "err" — here we pin down the actual
        // cause: extractors index forecastDays.get(1) ("tomorrow") and blow up when it's absent.
        String jsonWithSingleDay = """
                { "forecast": { "forecastday": [
                    { "date": "2026-09-21", "day": { "mintemp_c": 5.0, "maxtemp_c": 15.0, "avghumidity": 40, "maxwind_kph": 10.0 } }
                ] } }
                """;
        WeatherResponse r = response(jsonWithSingleDay);
        Function<WeatherResponse, Object> extractor = WeatherParameterRegistry.extractorFor("minTemp").orElseThrow();

        assertThrows(IndexOutOfBoundsException.class, () -> extractor.apply(r));
    }

    @Test
    void windDirectionIsMostFrequentAmongHours() {
        WeatherResponse r = response(TWO_DAYS_WITH_HOURS);
        Optional<Object> direction = WeatherParameterRegistry.extractWindDirection(r);
        assertEquals(Optional.of("NW"), direction);
    }

    @Test
    void windDirectionIsEmptyWhenTomorrowMissing() {
        String json = """
                { "forecast": { "forecastday": [
                    { "date": "2026-09-21", "day": { "mintemp_c": 5.0, "maxtemp_c": 15.0, "avghumidity": 40, "maxwind_kph": 10.0 } }
                ] } }
                """;
        WeatherResponse r = response(json);
        assertTrue(WeatherParameterRegistry.extractWindDirection(r).isEmpty());
    }

    @Test
    void windDirectionIsEmptyWhenHoursMissing() {
        String json = """
                { "forecast": { "forecastday": [
                    { "date": "2026-09-21", "day": { "mintemp_c": 5.0, "maxtemp_c": 15.0, "avghumidity": 40, "maxwind_kph": 10.0 } },
                    { "date": "2026-09-22", "day": { "mintemp_c": 10.5, "maxtemp_c": 20.5, "avghumidity": 55, "maxwind_kph": 18.0 } }
                ] } }
                """;
        WeatherResponse r = response(json);
        assertTrue(WeatherParameterRegistry.extractWindDirection(r).isEmpty());
    }

    @Test
    void extractorForWindDirectionReturnsMostFrequent() {
        // This goes through the same path the production code uses:
        // extractorFor("windDirection") -> r -> extractWindDirection(r).orElse(null).
        // Direct calls to extractWindDirection() would NOT catch a regression where
        // the lambda accidentally drops .orElse(null).
        WeatherResponse r = response(TWO_DAYS_WITH_HOURS);
        Function<WeatherResponse, Object> extractor =
                WeatherParameterRegistry.extractorFor("windDirection").orElseThrow();

        assertEquals("NW", extractor.apply(r));
    }


    @Test
    void windDirectionTieIsResolvedDeterministically() {
        // Two directions, one hour each. The implementation uses Stream.max(),
        // which returns the *first* encountered maximum for equal values.
        // We pin this down so future refactors don't silently change the winner.
        String json = """
            { "forecast": { "forecastday": [
                { "date": "2026-09-21", "day": { "mintemp_c": 5.0, "maxtemp_c": 15.0, "avghumidity": 40, "maxwind_kph": 10.0 } },
                {
                  "date": "2026-09-22",
                  "day": { "mintemp_c": 10.5, "maxtemp_c": 20.5, "avghumidity": 55, "maxwind_kph": 18.0 },
                  "hour": [
                    { "wind_dir": "NW", "wind_degree": 315 },
                    { "wind_dir": "N", "wind_degree": 350 }
                  ]
                }
              ] } }
            """;
        WeatherResponse r = response(json);

        Optional<Object> direction = WeatherParameterRegistry.extractWindDirection(r);

        assertEquals(Optional.of("NW"), direction);
    }

    @Test
    void windDirectionIsEmptyWhenHoursArrayIsEmpty() {
        String json = """
            { "forecast": { "forecastday": [
                { "date": "2026-09-21", "day": { "mintemp_c": 5.0, "maxtemp_c": 15.0, "avghumidity": 40, "maxwind_kph": 10.0 } },
                {
                  "date": "2026-09-22",
                  "day": { "mintemp_c": 10.5, "maxtemp_c": 20.5, "avghumidity": 55, "maxwind_kph": 18.0 },
                  "hour": []
                }
              ] } }
            """;
        WeatherResponse r = response(json);

        assertTrue(WeatherParameterRegistry.extractWindDirection(r).isEmpty());
    }

    @Test
    void windDirectionIgnoresNullDirections() {
        // One hour has null wind_dir — it must be filtered out and not affect the histogram.
        String json = """
            { "forecast": { "forecastday": [
                { "date": "2026-09-21", "day": { "mintemp_c": 5.0, "maxtemp_c": 15.0, "avghumidity": 40, "maxwind_kph": 10.0 } },
                {
                  "date": "2026-09-22",
                  "day": { "mintemp_c": 10.5, "maxtemp_c": 20.5, "avghumidity": 55, "maxwind_kph": 18.0 },
                  "hour": [
                    { "wind_dir": null, "wind_degree": 0 },
                    { "wind_dir": "NW", "wind_degree": 315 },
                    { "wind_dir": "NW", "wind_degree": 320 },
                    { "wind_dir": null, "wind_degree": 0 }
                  ]
                }
              ] } }
            """;
        WeatherResponse r = response(json);

        Optional<Object> direction = WeatherParameterRegistry.extractWindDirection(r);

        assertEquals(Optional.of("NW"), direction);
    }

      @Test
    void extractorForWindDirectionReturnsNullWhenNoHours() {
        String json = """
            { "forecast": { "forecastday": [
                { "date": "2026-09-21", "day": { "mintemp_c": 5.0, "maxtemp_c": 15.0, "avghumidity": 40, "maxwind_kph": 10.0 } },
                { "date": "2026-09-22", "day": { "mintemp_c": 10.5, "maxtemp_c": 20.5, "avghumidity": 55, "maxwind_kph": 18.0 } }
              ] } }
            """;
        WeatherResponse r = response(json);
        Function<WeatherResponse, Object> extractor =
                WeatherParameterRegistry.extractorFor("windDirection").orElseThrow();

        // No hours -> extractWindDirection is empty -> orElse(null) -> null.
        assertNull(extractor.apply(r));
    }
}
