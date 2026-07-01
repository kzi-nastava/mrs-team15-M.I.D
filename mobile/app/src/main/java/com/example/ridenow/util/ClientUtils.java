package com.example.ridenow.util;

import android.content.Context;
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
    private static TokenUtils tokenUtils;
    private static UnauthorizedListener unauthorizedListener;
    public interface UnauthorizedListener {
        void onUnauthorized();
    }

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request.Builder requestBuilder = chain.request().newBuilder();

                    // Add Authorization header if token is available
                    if (tokenUtils != null) {
                        String token = tokenUtils.getToken();
                        if (token != null && !token.isEmpty()) {
                            requestBuilder.addHeader("Authorization", "Bearer " + token);
                        }
                    }
                    return chain.proceed(requestBuilder.build());
                }
            })
            // Logging interceptor to log request and response details, and handle 401 Unauthorized responses
            .addInterceptor(new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request request = chain.request();
                    Log.i("ClientUtils", "HTTP " + request.method() + " " + request.url());
                    Response response = chain.proceed(request);
                    Log.i("ClientUtils", "Response: code=" + response.code() + " for " + request.url());

                    if (response.code() == 401 && tokenUtils != null) {
                        Log.w("ClientUtils", "Token expired or invalid, clearing auth data");
                        tokenUtils.clearAuthData();

                        if (unauthorizedListener != null) {
                            unauthorizedListener.onUnauthorized();
                        }
                    }
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

    public static void init(Context context) {
        tokenUtils = new TokenUtils(context.getApplicationContext());
    }
    public static TokenUtils getTokenUtils() {
        if (tokenUtils == null) {
            throw new IllegalStateException("ClientUtils not initialized. Call ClientUtils.init(context) first!");
        }
        return tokenUtils;
    }

    public static void setUnauthorizedListener(UnauthorizedListener listener) {
        unauthorizedListener = listener;
    }

    public static <T> T getClient(Class<T> service) {
        return retrofit.create(service);
    }

    @Deprecated
    public static void setAuthToken(String authToken) {
        if (tokenUtils != null) {
            String role = tokenUtils.getRole();
            long expiresAt = tokenUtils.getExpiresAt();
            boolean hasCurrentRide = tokenUtils.hasCurrentRide();
            tokenUtils.saveAuthData(authToken, role, expiresAt, hasCurrentRide);
        }
    }

    @Deprecated
    public static void setRole(String role) {
        if (tokenUtils != null) {
            String token = tokenUtils.getToken();
            long expiresAt = tokenUtils.getExpiresAt();
            boolean hasCurrentRide = tokenUtils.hasCurrentRide();
            tokenUtils.saveAuthData(token, role, expiresAt, hasCurrentRide);
        }
    }

    @Deprecated
    public static String getRole() {
        return tokenUtils != null ? tokenUtils.getRole() : null;
    }

    @Deprecated
    public static long getExpiresAt() {
        return tokenUtils != null ? tokenUtils.getExpiresAt() : 0;
    }

    @Deprecated
    public static void setExpiresAt(long expiresAt) {
        if (tokenUtils != null) {
            String token = tokenUtils.getToken();
            String role = tokenUtils.getRole();
            boolean hasCurrentRide = tokenUtils.hasCurrentRide();
            tokenUtils.saveAuthData(token, role, expiresAt, hasCurrentRide);
        }
    }

    public static String getServerBaseUrl() {
        if (BASE_URL.endsWith("/api/") || BASE_URL.endsWith("/api")) {
            return BASE_URL.replace("/api/", "").replace("/api", "");
        }
        return BASE_URL;
    }
}