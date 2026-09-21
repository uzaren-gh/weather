package org.weather.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

@Getter
public class WeatherResponse {

    private Location location;
    private Forecast forecast;

    @Getter
    public static class Location {
        private String name;
        private String country;
    }

    @Getter
    public static class Forecast {
        @SerializedName("forecastday")
        private List<ForecastDay> forecastDays;
    }

    @Getter
    public static class ForecastDay {
        private String date;
        private Day day;

        @SerializedName("hour")
        private List<Hour> hours;
    }

    @Getter
    public static class Hour {
        @SerializedName("wind_dir")
        private String windDir;

        @SerializedName("wind_degree")
        private int windDegree;
    }

    @Getter
    public static class Day {
        @SerializedName("mintemp_c")
        private double minTempC;

        @SerializedName("maxtemp_c")
        private double maxTempC;

        @SerializedName("avghumidity")
        private int avgHumidity;


        @SerializedName("maxwind_kph")
        private double maxWindKph;
    }
}