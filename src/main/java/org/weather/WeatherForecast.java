package org.weather;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.weather.model.WeatherResponse;
import org.weather.print.TablePrinter;
import org.weather.properties.WeatherProperties;
import org.weather.properties.WeatherPropertyLoader;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class WeatherForecast {

    public static void main(String[] args) {

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        log.info("Welcome to org.weather.WeatherForecast");

        WeatherProperties config = WeatherPropertyLoader.load();

        WeatherService service = new WeatherService(config.apiKey());

        Map<String, WeatherResponse> results = new LinkedHashMap<>();
        for (String city : config.cities()) {
            log.info("Fetching forecast for '{}'", city);
            try {
                WeatherResponse response = service.getTomorrowForecast(city);
                if (response == null) {
                    log.warn("Empty response for '{}'", city);
                    continue;
                }
                results.put(city, response);
            } catch (Exception e) {
                log.error("Failed to fetch forecast for '{}': {}", city, e.getMessage());
            }
        }

        TablePrinter.print(config.parameters(), results);

    }


}