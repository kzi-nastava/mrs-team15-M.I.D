package com.example.ridenow.util;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClientUtils {
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static String authToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTc2OTM0MTkwNywiZXhwIjoxNzY5MzQ1NTA3fQ.2o4h7-laSO0bJS9iDiayHXbLBRBuzd49AoUTQ7hHK7E";
    private static String role = null;

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
}
