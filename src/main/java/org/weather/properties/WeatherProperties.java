package org.weather.properties;

import java.util.List;

public record WeatherProperties(List<String> cities, List<String> parameters, String apiKey) {

    public WeatherProperties {
        cities = List.copyOf(cities);
        parameters = List.copyOf(parameters);
        if (apiKey == null) {
            throw new IllegalArgumentException("apiKey must not be null");
        }
    }
}