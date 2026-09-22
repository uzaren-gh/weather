package org.weather.properties;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeatherPropertiesTest {

    @Test
    void storesGivenValues() {
        WeatherProperties props = new WeatherProperties(
                List.of("Kyiv", "Madrid"),
                List.of("minTemp", "maxTemp"),
                "api-key");

        assertEquals(List.of("Kyiv", "Madrid"), props.cities());
        assertEquals(List.of("minTemp", "maxTemp"), props.parameters());
        assertEquals("api-key", props.apiKey());
    }

    @Test
    void copiesListsDefensively() {
        List<String> cities = new ArrayList<>(List.of("Kyiv"));
        List<String> parameters = new ArrayList<>(List.of("minTemp"));

        WeatherProperties props = new WeatherProperties(cities, parameters, "api-key");

        cities.add("Madrid");
        parameters.add("maxTemp");

        assertEquals(List.of("Kyiv"), props.cities());
        assertEquals(List.of("minTemp"), props.parameters());
    }

    @Test
    void resultingListsAreImmutable() {
        WeatherProperties props = new WeatherProperties(List.of("Kyiv"), List.of("minTemp"), "api-key");

        assertThrows(UnsupportedOperationException.class, () -> props.cities().add("Madrid"));
        assertThrows(UnsupportedOperationException.class, () -> props.parameters().add("maxTemp"));
    }

    @Test
    void rejectsNullApiKey() {
        assertThrows(IllegalArgumentException.class,
                () -> new WeatherProperties(List.of("Kyiv"), List.of("minTemp"), null));
    }
}
