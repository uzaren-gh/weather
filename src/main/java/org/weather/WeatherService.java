package org.weather;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.weather.api.WeatherApi;
import org.weather.model.WeatherResponse;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class WeatherService {

    private static final String BASE_URL = "https://api.weatherapi.com/v1/";
    private static final int FORECAST_DAYS = 2; // 0 — today, 1 — tomorrow

    private final WeatherApi api;
    private final String apiKey;

    public WeatherService(String apiKey) {
        this.apiKey = apiKey;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);//BODY?

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.api = retrofit.create(WeatherApi.class);
    }

    public WeatherResponse getTomorrowForecast(String location) throws IOException {
        Call<WeatherResponse> call = api.getForecast(apiKey, location, FORECAST_DAYS);
        return call.execute().body();
    }
}