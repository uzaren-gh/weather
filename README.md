# Weather Forecast

A console Java application that fetches tomorrow's weather forecast for a list of cities via [WeatherAPI](https://www.weatherapi.com/) and prints the result as a table.

## Features

- Fetches tomorrow's forecast for multiple cities in a single run.
- Configurable set of output parameters: `minTemp`, `maxTemp`, `humidity`, `windSpeed`, `windDirection`.
- Wind direction (`windDirection`) is computed as the most frequent value among the day's hourly data.
- The list of cities and parameters is set in a config file; unsupported parameters are ignored with a warning logged.
- Logging via SLF4J/Logback: to the console (WARN and above) and to a `weather-forecast.log` file with daily rotation.
- Resilient to partial failures: if the forecast for one city can't be fetched, or one parameter can't be computed, the application doesn't fail as a whole — that cell/row shows `err` or `n/a` while the other cities are still printed. The guiding principle is "do as much as you can": one failure shouldn't take down the whole report.

## Tech stack

- Java 21
- Gradle
- Retrofit 2 + Gson (HTTP client for WeatherAPI)
- OkHttp logging-interceptor
- Lombok
- SLF4J / Logback
- JUnit 5

## Requirements

- JDK 21+
- An API key from [weatherapi.com](https://www.weatherapi.com/)

## Configuration

1. Set the API key via the `WEATHER_API_KEY` environment variable:

   ```bash
   export WEATHER_API_KEY=your_key
   ```

   On Windows (PowerShell):
   ```powershell
   $env:WEATHER_API_KEY = "your_key"
   ```

2. Edit `src/main/resources/weather.properties`:

   ```properties
   weather.api.baseUrl=https://api.weatherapi.com/v1/
   cities=Chisinau,Madrid,Kyiv,Amsterdam,Odesa
   weather.params=minTemp,maxTemp,humidity,windSpeed,windDirection
   ```

   - `cities` — a comma-separated list of cities (passed to WeatherAPI as-is).
   - `weather.params` — a comma-separated list of output columns; allowed values are listed above. Unsupported values are ignored with a warning.
   - Duplicate values are allowed but a warning is logged.
   
For development, the file lives in src/main/resources/weather.properties. 
It is excluded from the fat JAR and must be supplied externally at runtime (see below).

## Running

```bash
./gradlew run
```

On Windows:

```bash
gradlew.bat run
```

## Building a fat JAR
```bash
./gradlew shadowJar
```
The result is build/libs/weather-<version>-all.jar — a self-contained JAR with all runtime dependencies.

## Running the fat JAR
The weather.properties and logback.xml file is not packaged inside the JAR (see the processResources { exclude ... } block in build.gradle).
It must be placed next to the JAR before running:

app/
├── weather-project-1.0.0-all.jar
└── weather.properties
└── logback.xml

or

app/
├── weather-project-1.0.0-all.jar
└── config 
      └──weather.properties
      └──logback.xml

Then:

```bash
java -jar weather-project-1.0.0-all.jar
```

How the config file is located

The loader looks for weather.properties in the following order and uses the first readable one:

    The directory passed via -Dweather.config=/path/to/dir (highest priority).

    The current working directory (.).

    The config/ subdirectory of the current working directory.

    The parent directory (..).

    The config/ subdirectory of the parent directory.

    The classpath (fallback, e.g. when the file is bundled).

This means the same JAR works in IDEA (cwd = project root), in a shell (cwd = folder with the JAR), and in a distribution layout with a dedicated config/ folder.

## Sample output

```
+-----------+---------+---------+----------+-----------+---------------+
| City      | minTemp | maxTemp | humidity | windSpeed | windDirection |
+-----------+---------+---------+----------+-----------+---------------+
| Chisinau  | 12.0    | 21.0    | 65       | 18.0      | NW            |
| Madrid    | 15.0    | 27.0    | 40       | 12.0      | SW            |
+-----------+---------+---------+----------+-----------+---------------+
```

## Error handling

The application follows a "do as much as you can" principle:

    If the forecast for one city fails (network error, invalid city name, API error), the row for that city is skipped and a warning is logged. Other cities are still printed.

    If a single parameter can't be extracted (e.g. hourly data is missing, so windDirection can't be computed), the cell shows n/a.

    If an extractor throws (e.g. the API returned fewer days than expected), the cell shows err.

The report is always printed, even if some data is missing — a single failure never takes down the whole run.


## Project structure

```
src/main/java/org/weather/
├── WeatherForecast.java              # entry point
├── WeatherService.java                # HTTP client (Retrofit) for WeatherAPI
├── api/WeatherApi.java                # REST endpoint definition
├── model/WeatherResponse.java         # API response DTO
├── properties/
│   ├── WeatherProperties.java         # application configuration (record)
│   ├── WeatherPropertyLoader.java     # reads weather.properties + environment variables
│   └── WeatherParameterRegistry.java  # registry of supported parameters and their extractors
└── print/TablePrinter.java            # formatted console table output
```

## Tests

```bash
./gradlew test
```
