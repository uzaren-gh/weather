package org.weather.properties;

import org.weather.model.WeatherResponse;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WeatherParameterRegistry {
    private WeatherParameterRegistry() {
    }

    private static final Map<String, Function<WeatherResponse, Object>> EXTRACTORS = Map.of(
            "minTemp", r -> day(r).getMinTempC(),
            "maxTemp", r -> day(r).getMaxTempC(),
            "humidity", r -> day(r).getAvgHumidity(),
            "windSpeed", r -> day(r).getMaxWindKph(),
            "windDirection", r-> extractWindDirection(r).orElse(null)
    );


    private static WeatherResponse.Day day(WeatherResponse r) {
        return r.getForecast().getForecastDays().get(1).getDay();
    }

    public static Optional<Function<WeatherResponse, Object>> extractorFor(String key) {
        return Optional.ofNullable(EXTRACTORS.get(key));
    }

    public static Set<String> supportedKeys() {
        return EXTRACTORS.keySet();
    }

    public static Optional<Object> extractWindDirection(WeatherResponse r) {
        List<WeatherResponse.ForecastDay> days = r.getForecast().getForecastDays();
        if (days == null || days.size() < 2) return Optional.empty();

        List<WeatherResponse.Hour> hours = days.get(1).getHours();
        if (hours == null || hours.isEmpty()) return Optional.empty();

        Map<String, Long> histogram = hours.stream()
                .map(WeatherResponse.Hour::getWindDir)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return histogram.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
}