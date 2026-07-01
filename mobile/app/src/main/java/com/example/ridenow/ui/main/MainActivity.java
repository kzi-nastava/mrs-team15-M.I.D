package com.example.ridenow.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.ridenow.R;
import com.example.ridenow.dto.driver.DriverStatusRequestDTO;
import com.example.ridenow.dto.driver.DriverStatusResponseDTO;
import com.example.ridenow.dto.enums.DriverStatus;
import com.example.ridenow.service.DriverService;
import com.example.ridenow.service.LogoutService;
import com.example.ridenow.service.TokenExpirationService;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.TokenUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TokenExpirationService tokenExpirationService;
    private NavController navController;
    private SwitchMaterial switchDriverStatus;
    private View driverStatusContainer;
    private TextView tvDriverStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ClientUtils.init(this);

        requestNotificationPermission();


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
        navigationView = findViewById(R.id.navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            TokenUtils tokenUtils = ClientUtils.getTokenUtils();
            if ("DRIVER".equals(tokenUtils.getRole())) {
                fetchDriverStatus();
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            Log.d(TAG, "Clicked item id: " + item.getItemId() + " title: " + item.getTitle());
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

        View headerView = navigationView.getHeaderView(0);
        driverStatusContainer = headerView.findViewById(R.id.driverStatusContainer);
        switchDriverStatus = headerView.findViewById(R.id.switchDriverStatus);
        tvDriverStatus = headerView.findViewById(R.id.tvDriverStatus);

        switchDriverStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            updateDriverStatus(isChecked);
        });

        toggle.syncState();
        setupTokenUtils();
        updateMenuVisibility();

        // Handle notification click from FCM
        if (getIntent().getBooleanExtra("navigateToNotifications", false)) {
            navController.navigate(R.id.notifications);
        }

        handleDeepLink(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null) return;

        Uri data = intent.getData();
        if (data == null) return;

        if ("ridenow".equals(data.getScheme()) && "driver-activation".equals(data.getHost())) {
            String token = data.getLastPathSegment();
            if (token != null && !token.isEmpty()) {
                navigateToDriverActivation(token);
            }
        }
    }

    private void navigateToDriverActivation(String token) {
        Bundle args = new Bundle();
        args.putString("token", token);  
        try {
            navController.navigate(R.id.driver_activation, args);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to driver activation screen", e);
        }
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

        // Initialize Firebase Cloud Messaging token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d(TAG, "FCM token obtained successfully: " + token);
                        // Send token to backend
                        registerFcmToken(token);
                    } else {
                        Log.e(TAG, "Failed to get FCM token", task.getException());
                    }
                });

        updateMenuVisibility();
    }

    private void registerFcmToken(String token) {
        try {
            com.example.ridenow.dto.user.FcmTokenDTO tokenDTO = new com.example.ridenow.dto.user.FcmTokenDTO();
            tokenDTO.setToken(token);

            com.example.ridenow.service.UserService userService = ClientUtils.getClient(com.example.ridenow.service.UserService.class);
            userService.registerToken(tokenDTO).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "FCM token registered successfully with backend");
                    } else {
                        Log.e(TAG, "Failed to register FCM token: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<Void> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error registering FCM token", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error registering FCM token", e);
        }
    }

    private void updateMenuVisibility() {
        if (navigationView == null) {
            return;
        }

        TokenUtils tokenUtils = ClientUtils.getTokenUtils();
        boolean isLoggedIn = tokenUtils.isLoggedIn();
        String userRole = tokenUtils.getRole();

        // Get menu and hide/show items based on user role
        navigationView.getMenu().findItem(R.id.nav_home).setVisible(true);

        // Authentication related items
        navigationView.getMenu().findItem(R.id.login).setVisible(!isLoggedIn);
        //navigationView.getMenu().findItem(R.id.driver_activation).setVisible(!isLoggedIn);
        //navigationView.getMenu().findItem(R.id.registration).setVisible(!isLoggedIn);
        navigationView.getMenu().findItem(R.id.reset_password).setVisible(!isLoggedIn);
        navigationView.getMenu().findItem(R.id.nav_home).setVisible(!isLoggedIn);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(isLoggedIn);

        // Role-specific items
        boolean isDriver = "DRIVER".equals(userRole);
        boolean isUser = "USER".equals(userRole);
        boolean isAdmin = "ADMIN".equals(userRole);

        driverStatusContainer.setVisibility(isDriver ? View.VISIBLE : View.GONE);
        if (isDriver) {
            fetchDriverStatus();
        }

        // Driver-only items
        navigationView.getMenu().findItem(R.id.history).setVisible(isDriver); // Driver History
        navigationView.getMenu().findItem(R.id.upcoming_rides).setVisible(isDriver || isUser); // Upcoming Rides
        navigationView.getMenu().findItem(R.id.driver_profile).setVisible(isDriver);
        navigationView.getMenu().findItem(R.id.driver_report).setVisible(isDriver);

        // User-only items
        navigationView.getMenu().findItem(R.id.profile).setVisible(isUser);
        navigationView.getMenu().findItem(R.id.ride_ordering).setVisible(isUser);
        navigationView.getMenu().findItem(R.id.passenger_history).setVisible(isUser);
        navigationView.getMenu().findItem(R.id.passenger_report).setVisible(isUser);

        // Common logged-in user items
        //navigationView.getMenu().findItem(R.id.change_password).setVisible(isLoggedIn);

        // Non-admin items
        navigationView.getMenu().findItem(R.id.current_ride).setVisible(isUser || isDriver); // User Management

        // Admin-only items
        navigationView.getMenu().findItem(R.id.driver_requests).setVisible(isAdmin); // Driver Requests
        navigationView.getMenu().findItem(R.id.driver_registration).setVisible(isAdmin); // Driver Registration
        navigationView.getMenu().findItem(R.id.admin_chats).setVisible(isAdmin); // Support Chats
        navigationView.getMenu().findItem(R.id.admin_users).setVisible(isAdmin); // Admin users
        navigationView.getMenu().findItem(R.id.admin_report).setVisible(isAdmin); // Admin Reports

        // Live support for logged-in non-admin users
        navigationView.getMenu().findItem(R.id.live_support).setVisible(isLoggedIn && !isAdmin);
        navigationView.getMenu().findItem(R.id.notifications).setVisible(isLoggedIn && !isAdmin);
        navigationView.getMenu().findItem(R.id.active_rides).setVisible(isAdmin); // Active Rides
        navigationView.getMenu().findItem(R.id.price_configs).setVisible(isAdmin); // Price Configuration
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
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
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
        updateMenuVisibility();
        if (navController != null) {
            try {
                navController.navigate(R.id.login);
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to login during logout", e);
            }
        }
    }

    private void fetchDriverStatus() {
        DriverService driverService = ClientUtils.getClient(DriverService.class);
        driverService.getDriverStatus().enqueue(new Callback<DriverStatusResponseDTO>() {
            @Override
            public void onResponse(Call<DriverStatusResponseDTO> call, Response<DriverStatusResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applyStatusToUi(response.body().getStatus(), response.body().getPendingStatus());
                }
            }
            @Override
            public void onFailure(Call<DriverStatusResponseDTO> call, Throwable t) { }
        });
    }

    private void updateDriverStatus(boolean active) {
        DriverStatusRequestDTO dto = new DriverStatusRequestDTO();
        dto.setStatus(active ? DriverStatus.ACTIVE : DriverStatus.INACTIVE);

        DriverService driverService = ClientUtils.getClient(DriverService.class);
        driverService.changeDriverStatus(dto).enqueue(new Callback<DriverStatusResponseDTO>() {
            @Override
            public void onResponse(Call<DriverStatusResponseDTO> call, Response<DriverStatusResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applyStatusToUi(response.body().getStatus(), response.body().getPendingStatus());
                    Toast.makeText(MainActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                } else {
                    fetchDriverStatus();
                }
            }
            @Override
            public void onFailure(Call<DriverStatusResponseDTO> call, Throwable t) {
                fetchDriverStatus();
            }
        });
    }
    private void applyStatusToUi(DriverStatus status, DriverStatus pendingStatus) {
        boolean checked = status == DriverStatus.ACTIVE;
        switchDriverStatus.setChecked(checked);
        tvDriverStatus.setText(checked ? "Active" : "Inactive");
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }
}