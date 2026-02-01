package com.example.ridenow.util;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClientUtils {
    private static final String BASE_URL = "http://10.0.2.2:8081/api/";
    private static String authToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkcml2ZXIyQGdtYWlsLmNvbSIsImlhdCI6MTc2OTk3MTI4MywiZXhwIjoxNzY5OTc0ODgzfQ.qByPeCY6jXVS7XByNsR3gs6uoG_fc-ejiZ9i0JHoIwc";
    private static String role = "DRIVER";

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request.Builder requestBuilder = chain.request().newBuilder();

                    if (authToken != null) {
                        requestBuilder.addHeader("Authorization", "Bearer " + authToken);
                    }

                    return chain.proceed(requestBuilder.build());
                }
            })
            // Simple logger interceptor to print request URL and response code to Logcat
            .addInterceptor(new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request request = chain.request();
                    Log.i("ClientUtils", "HTTP " + request.method() + " " + request.url());
                    Response response = chain.proceed(request);
                    Log.i("ClientUtils", "Response: code=" + response.code() + " for " + request.url());
                    return response;
                }
            })
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();

    public static <T> T getClient(Class<T> service) {
        return retrofit.create(service);
    }

    // Return server root (without the "/api/" suffix) so relative image paths can be resolved
    public static String getServerBaseUrl() {
        if (BASE_URL.endsWith("/api/") || BASE_URL.endsWith("/api")) {
            return BASE_URL.replace("/api/", "").replace("/api", "");
        }
        return BASE_URL;
    }
}
