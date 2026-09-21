package org.weather.properties;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class WeatherPropertyLoader {

    private static final String CONFIG_FILE = "weather.properties";
    private static final String API_KEY = System.getenv("WEATHER_API_KEY");

    private WeatherPropertyLoader() {
    }

    public static WeatherProperties load() {
        Properties props = readProperties();
        return new WeatherProperties(
                parseList("cities", props.getProperty("cities", "")),
                parseAndValidateParams(props.getProperty("weather.params", "")),
                API_KEY
        );
    }

    private static Properties readProperties() {
        Properties props = new Properties();
        try (InputStream in = WeatherPropertyLoader.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (in == null) {
                throw new IllegalStateException(
                        "Config file not found in classpath: " + CONFIG_FILE);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
        return props;
    }

    private static List<String> parseList(String key, String raw) {
        List<String> values = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        warnOnDuplicates(key, values);
        return values;
    }

    private static void warnOnDuplicates(String key, List<String> values) {
        if (new HashSet<>(values).size() != values.size()) {
            log.warn("Duplicate values found in '{}': {}", key, values);
        }
    }

    private static List<String> parseAndValidateParams(String raw) {
        List<String> params = parseList("weather.params", raw);
        Set<String> supported = WeatherParameterRegistry.supportedKeys();

        List<String> valid = new ArrayList<>();
        for (String p : params) {
            if (supported.contains(p)) {
                valid.add(p);
            } else {
                log.warn("Unsupported weather parameter '{}', it will be ignored. Supported: {}",
                        p, supported);
            }
        }
        return valid;
    }
}