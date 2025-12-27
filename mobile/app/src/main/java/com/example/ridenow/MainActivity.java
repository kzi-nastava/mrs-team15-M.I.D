package com.example.ridenow;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int statusBarHeight = insets.getSystemWindowInsetTop();

            // Adjust toolbar layout params to include status bar height
            ViewGroup.LayoutParams params = v.getLayoutParams();
            int actionBarHeight = getResources().getDimensionPixelSize(
                    androidx.appcompat.R.dimen.abc_action_bar_default_height_material);
            params.height = actionBarHeight + statusBarHeight;
            v.setLayoutParams(params);

            // Add top padding for content
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

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);

        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
