package com.example.ridenow.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.ridenow.util.TokenUtils;

public class TokenExpirationService {
    private static final String TAG = "TokenExpirationService";
    private static final long CHECK_INTERVAL = 30000;
    private final TokenUtils tokenUtils;
    private final Context context;
    private final Handler handler;
    private final Runnable checkRunnable;
    private boolean isChecking = false;
    private TokenExpiredListener listener;

    public interface TokenExpiredListener {
        void onTokenExpired();
    }

    public TokenExpirationService(Context context, TokenUtils tokenUtils) {
        this.context = context.getApplicationContext();
        this.tokenUtils = tokenUtils;
        this.handler = new Handler(Looper.getMainLooper());

        this.checkRunnable = new Runnable() {
            @Override
            public void run() {
                checkTokenExpiration();
                if (isChecking) {
                    handler.postDelayed(this, CHECK_INTERVAL);
                }
            }
        };
    }

    public void setTokenExpiredListener(TokenExpiredListener listener) {
        this.listener = listener;
    }

    public void startTokenExpirationCheck() {
        if (!isChecking) {
            isChecking = true;
            Log.d(TAG, "Starting token expiration checks");
            handler.post(checkRunnable);
        }
    }

    public void stopTokenExpirationCheck() {
        if (isChecking) {
            isChecking = false;
            handler.removeCallbacks(checkRunnable);
            Log.d(TAG, "Stopped token expiration checks");
        }
    }

    private void checkTokenExpiration() {
        long expiresAt = tokenUtils.getExpiresAt();

        if (expiresAt == 0) {
            return;
        }

        long now = System.currentTimeMillis();

        if (now >= expiresAt) {
            handleExpiredToken();
        } else {
            long timeLeft = expiresAt - now;
            Log.d(TAG, "Token valid for " + (timeLeft / 1000) + " more seconds");
        }
    }

    private void handleExpiredToken() {
        Log.w(TAG, "Token has expired, logging out");
        stopTokenExpirationCheck();
        tokenUtils.clearAuthData();

        Toast.makeText(context, "Your session has expired. Please log in again.", Toast.LENGTH_LONG).show();

        if (listener != null) {
            listener.onTokenExpired();
        }
    }
}