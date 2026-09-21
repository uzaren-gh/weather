package org.weather;

import lombok.extern.slf4j.Slf4j;
import org.weather.properties.WeatherProperties;
import org.weather.properties.WeatherPropertyLoader;

@Slf4j
public class WeatherForecast {

    public static void main(String[] args) {

        log.info("Welcome to org.weather.WeatherForecast");

        WeatherProperties config = WeatherPropertyLoader.load();
        log.info("Cities: {}", config.cities());
        log.info("Parameters: {}", config.parameters());
    }
}