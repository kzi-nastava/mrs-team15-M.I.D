package com.example.ridenow.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenUtils {
    private static final String PREFS_NAME = "RideNowPrefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EXPIRES_AT = "expires_at";
    private static final String KEY_HAS_CURRENT_RIDE = "has_current_ride";

    private final SharedPreferences prefs;

    public TokenUtils(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    public void saveAuthData(String token, String role, long expiresAt, boolean hasCurrentRide){
        prefs.edit()
                .putString(KEY_AUTH_TOKEN, token).putString(KEY_ROLE, role)
                .putLong(KEY_EXPIRES_AT, expiresAt)
                .putBoolean(KEY_HAS_CURRENT_RIDE, hasCurrentRide).apply();
    }

    public void clearAuthData(){
        prefs.edit()
                .remove(KEY_AUTH_TOKEN).remove(KEY_ROLE)
                .remove(KEY_EXPIRES_AT).remove(KEY_HAS_CURRENT_RIDE).apply();
    }

    public String getToken(){
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    public String getRole(){
        return  prefs.getString(KEY_ROLE, null);
    }

    public long getExpiresAt(){
        return prefs.getLong(KEY_EXPIRES_AT, 0);
    }

    public  boolean hasCurrentRide(){
        return prefs.getBoolean(KEY_HAS_CURRENT_RIDE, false);
    }

    public  boolean isTokenValid(){
        String token = getToken();
        long expiresAt = getExpiresAt();

        if(token == null || token.isEmpty()){
            return false;
        }
        return System.currentTimeMillis() < expiresAt;
    }

    public boolean isLoggedIn(){
        return isTokenValid();
    }
}