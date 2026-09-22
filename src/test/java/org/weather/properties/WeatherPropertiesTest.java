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
                "api-key",
                "base-url");

        assertEquals(List.of("Kyiv", "Madrid"), props.cities());
        assertEquals(List.of("minTemp", "maxTemp"), props.parameters());
        assertEquals("api-key", props.apiKey());
        assertEquals("base-url", props.baseUrl());
    }

    @Test
    void copiesListsDefensively() {
        List<String> cities = new ArrayList<>(List.of("Kyiv"));
        List<String> parameters = new ArrayList<>(List.of("minTemp"));

        WeatherProperties props = new WeatherProperties(cities, parameters, "api-key", "base-url");

        cities.add("Madrid");
        parameters.add("maxTemp");

        assertEquals(List.of("Kyiv"), props.cities());
        assertEquals(List.of("minTemp"), props.parameters());
    }

    @Test
    void resultingListsAreImmutable() {
        WeatherProperties props = new WeatherProperties(List.of("Kyiv"), List.of("minTemp"), "api-key", "base-url");

        assertThrows(UnsupportedOperationException.class, () -> props.cities().add("Madrid"));
        assertThrows(UnsupportedOperationException.class, () -> props.parameters().add("maxTemp"));
    }

    @Test
    void rejectsNullApiKey() {
        assertThrows(IllegalArgumentException.class,
                () -> new WeatherProperties(List.of("Kyiv"), List.of("minTemp"), null, "base-url"));
    }
}
