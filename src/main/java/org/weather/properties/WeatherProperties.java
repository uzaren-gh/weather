package org.weather.properties;

import java.util.List;

public record WeatherProperties(List<String> cities, List<String> parameters) {

    public WeatherProperties(List<String> cities, List<String> parameters) {
        this.cities = List.copyOf(cities);
        this.parameters = List.copyOf(parameters);
    }
}