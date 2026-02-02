package com.example.ridenow.ui.main;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.ridenow.R;
import com.example.ridenow.service.LogoutService;
import com.example.ridenow.service.TokenExpirationService;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.TokenUtils;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private TokenExpirationService tokenExpirationService;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ClientUtils.init(this);

        Toolbar toolbar = findViewById(R.id.toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int statusBarHeight = insets.getSystemWindowInsetTop();

            ViewGroup.LayoutParams params = v.getLayoutParams();
            int actionBarHeight = getResources().getDimensionPixelSize(
                    androidx.appcompat.R.dimen.abc_action_bar_default_height_material);
            params.height = actionBarHeight + statusBarHeight;
            v.setLayoutParams(params);

            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());

            return insets;
        });

        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                handleLogout();
                drawerLayout.closeDrawers();
                return true;
            }
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawerLayout.closeDrawers();
            }
            return handled;
        });
        toggle.syncState();
        setupTokenUtils();
    }

    private void setupTokenUtils() {
        TokenUtils tokenUtils = ClientUtils.getTokenUtils();

        tokenExpirationService = new TokenExpirationService(this, tokenUtils);
        tokenExpirationService.setTokenExpiredListener(() -> {
            Log.w(TAG, "Token expired, redirecting to login");
            runOnUiThread(() -> {
                try {
                    navController.navigate(R.id.login);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to login", e);
                }
            });
        });

        ClientUtils.setUnauthorizedListener(() -> {
            Log.w(TAG, "Unauthorized response, redirecting to login");
            runOnUiThread(() -> {
                try {
                    navController.navigate(R.id.login);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to login", e);
                }
            });
        });

        if (tokenUtils.isLoggedIn()) {
            Log.d(TAG, "User is logged in, starting token expiration checks");
            tokenExpirationService.startTokenExpirationCheck();
        } else {
            Log.d(TAG, "User not logged in");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tokenExpirationService != null) {
            tokenExpirationService.stopTokenExpirationCheck();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onLoginSuccess() {
        if (tokenExpirationService != null) {
            Log.d(TAG, "Login success, starting token expiration checks");
            tokenExpirationService.startTokenExpirationCheck();
        }
    }

    public void handleLogout() {
        LogoutService.logout(new LogoutService.LogoutCallback() {
            @Override
            public void onLogoutSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    onLogout();
                });
            }

            @Override
            public void onLogoutFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Logout failed: " + error, Toast.LENGTH_SHORT).show();
                    onLogout();
                });
            }
        });
    }

    public void onLogout() {
        Log.d(TAG, "Logging out user");
        if (tokenExpirationService != null) {
            tokenExpirationService.stopTokenExpirationCheck();
        }
        ClientUtils.getTokenUtils().clearAuthData();
        if (navController != null) {
            try {
                navController.navigate(R.id.login);
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to login on logout", e);
            }
        }
    }
}