package com.doubleangels.nextdnsmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doubleangels.nextdnsmanagement.adaptors.PermissionsAdapter;
import com.doubleangels.nextdnsmanagement.protocol.VisualIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        // Get SharedPreferences for storing app preferences
        SharedPreferences sharedPreferences = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);

        try {
            // Setup toolbar
            setupToolbarForActivity();
            // Setup language/locale
            String appLocale = setupLanguageForActivity();
            // Setup dark mode
            setupDarkModeForActivity(sharedPreferences);
            // Setup visual indicator
            setupVisualIndicatorForActivity(this);
        } catch (Exception e) {
        }

        // Setup RecyclerView for displaying permissions list
        RecyclerView recyclerView = findViewById(R.id.permissionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get list of permissions and set up RecyclerView adapter
        List<PermissionInfo> permissions = getPermissionsList();
        PermissionsAdapter adapter = new PermissionsAdapter(permissions);
        recyclerView.setAdapter(adapter);
    }

    // Setup toolbar for the activity
    private void setupToolbarForActivity() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        // Setup click listener for connection status ImageView
        ImageView imageView = findViewById(R.id.connectionStatus);
        imageView.setOnClickListener(v -> startActivity(new Intent(this, StatusActivity.class)));
    }

    // Setup language/locale for the activity
    private String setupLanguageForActivity() {
        Configuration config = getResources().getConfiguration();
        Locale appLocale = config.getLocales().get(0);
        Locale.setDefault(appLocale);
        Configuration newConfig = new Configuration(config);
        newConfig.setLocale(appLocale);
        new ContextThemeWrapper(getBaseContext(), R.style.AppTheme).applyOverrideConfiguration(newConfig);
        return appLocale.getLanguage();
    }

    // Setup dark mode for the activity based on user preferences
    private void setupDarkModeForActivity(SharedPreferences sharedPreferences) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            String darkMode = sharedPreferences.getString("dark_mode", "match");
            if (darkMode.contains("match")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else if (darkMode.contains("on")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }

    // Setup visual indicator for the activity
    private void setupVisualIndicatorForActivity(LifecycleOwner lifecycleOwner) {
        try {
            new VisualIndicator(this).initialize(this, lifecycleOwner, this);
        } catch (Exception e) {
        }
    }

    // Retrieve the list of permissions requested by the app
    private List<PermissionInfo> getPermissionsList() {
        List<PermissionInfo> permissions = new ArrayList<>();
        try {
            // Get package info including requested permissions
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);

            if (packageInfo.requestedPermissions != null) {
                // Retrieve PermissionInfo for each requested permission and add to list
                for (String permission : packageInfo.requestedPermissions) {
                    PermissionInfo permissionInfo = getPackageManager().getPermissionInfo(permission, 0);
                    permissions.add(permissionInfo);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return permissions;
    }

    // Inflate menu for the activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_back_only, menu);
        return true;
    }

    // Handle menu item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.back) {
            // Navigate back to SettingsActivity
            Intent mainIntent = new Intent(this, SettingsActivity.class);
            startActivity(mainIntent);
        }
        return super.onContextItemSelected(item);
    }
}
