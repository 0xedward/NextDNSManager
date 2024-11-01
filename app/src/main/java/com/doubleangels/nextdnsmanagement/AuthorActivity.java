package com.doubleangels.nextdnsmanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
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

import com.doubleangels.nextdnsmanagement.protocol.VisualIndicator;

import java.util.Locale;

public class AuthorActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);

        // Get shared preferences
        SharedPreferences sharedPreferences = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);

        try {

            // Setup toolbar
            setupToolbarForActivity();

            // Setup language for the activity
            String appLocale = setupLanguageForActivity();

            // Setup dark mode for the activity
            setupDarkModeForActivity(sharedPreferences);

            // Setup visual indicator for the activity
            setupVisualIndicatorForActivity(this);
        } catch (Exception e) {
        }
    }

    // Method to setup toolbar for the activity
    private void setupToolbarForActivity() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // Set onClickListener for connection status image
        ImageView imageView = findViewById(R.id.connectionStatus);
        imageView.setOnClickListener(v -> startActivity(new Intent(this, StatusActivity.class)));
    }

    // Method to setup language for the activity
    private String setupLanguageForActivity() {
        Configuration config = getResources().getConfiguration();
        Locale appLocale = config.getLocales().get(0);
        Locale.setDefault(appLocale);
        Configuration newConfig = new Configuration(config);
        newConfig.setLocale(appLocale);
        new ContextThemeWrapper(getBaseContext(), R.style.AppTheme).applyOverrideConfiguration(newConfig);
        return appLocale.getLanguage();
    }

    // Method to setup dark mode for the activity
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

    // Method to setup visual indicator for the activity
    private void setupVisualIndicatorForActivity(LifecycleOwner lifecycleOwner) {
        try {
            new VisualIndicator(this).initialize(this, lifecycleOwner, this);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_back_only, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks
        if (item.getItemId() == R.id.back) {
            // Navigate back to SettingsActivity
            Intent mainIntent = new Intent(this, SettingsActivity.class);
            startActivity(mainIntent);
        }
        return super.onContextItemSelected(item);
    }
}
