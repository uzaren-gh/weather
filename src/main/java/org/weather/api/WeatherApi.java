package org.weather.api;

import org.weather.model.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    /**
     * days=2 The parameter is mandatory.: forecastday[0] — today, forecastday[1] — tomorrow.
     *
     * @param apiKey
     * @param location
     * @param days
     */
    @GET("forecast.json")
    Call<WeatherResponse> getForecast(
            @Query("key") String apiKey,
            @Query("q") String location,
            @Query("days") int days
    );
}
