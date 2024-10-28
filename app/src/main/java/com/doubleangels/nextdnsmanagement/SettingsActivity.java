package com.doubleangels.nextdnsmanagement;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.doubleangels.nextdnsmanagement.protocol.VisualIndicator;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Get SharedPreferences for storing app preferences
        SharedPreferences sharedPreferences = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);

        try {
            // Setup toolbar
            setupToolbarForActivity();
            // Setup language/locale
            String appLocale = setupLanguageForActivity();
            // Setup dark mode
            setupDarkModeForActivity(sharedPreferences);
            // Initialize views (PreferenceFragment)
            initializeViews();
            // Setup visual indicator
            setupVisualIndicatorForActivity(this);
        } catch (Exception e) {
        }
    }

    // Setup toolbar for the activity
    private void setupToolbarForActivity() {
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
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
            VisualIndicator visualIndicator = new VisualIndicator(this);
            visualIndicator.initialize(this, lifecycleOwner, this);
        } catch (Exception e) {
        }
    }

    // Initialize views (PreferenceFragment)
    private void initializeViews() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commitNow();
    }

    // Inner class representing the preference fragment
    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Load preferences from XML resource
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            // Get SharedPreferences
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // Find the PreferenceCategory by its key
                PreferenceCategory darkModePreferenceCategory = findPreference("darkmode");
                if (darkModePreferenceCategory != null) {
                    // Hide the PreferenceCategory if SDK is below 33
                    darkModePreferenceCategory.setVisible(false);
                }
            } else {
                // Find preferences and set up listeners
                ListPreference darkModePreference = findPreference("dark_mode");
                assert darkModePreference != null;
                setupDarkModeChangeListener(darkModePreference, sharedPreferences);
            }
            // Set up click listeners for various buttons
            setupButton("whitelist_domain_1_button", R.string.whitelist_domain_1);
            setupButton("whitelist_domain_2_button", R.string.whitelist_domain_2);
            setupButtonForIntent("author_button");
            setupButton("github_button", R.string.github_url);
            setupButton("github_issue_button", R.string.github_issues_url);
            setupButton("donation_button", R.string.donation_url);
            setupButton("translate_button", R.string.translate_url);
            setupButton("privacy_policy_button", R.string.privacy_policy_url);
            setupButton("nextdns_privacy_policy_button", R.string.nextdns_privacy_policy_url);
            setupButton("nextdns_user_agreement_button", R.string.nextdns_user_agreement_url);
            setupButtonForIntent("permission_button");
            setupButton("version_button", R.string.versions_url);
            // Set version name as summary for version button
            String versionName = BuildConfig.VERSION_NAME;
            Preference versionPreference = findPreference("version_button");
            if (versionPreference != null) {
                versionPreference.setSummary(versionName);
            }
        }


        // Set up click listener for a button preference
        private void setupButton(String buttonKey, int textResource) {
            Preference button = findPreference(buttonKey);
            assert button != null;
            button.setOnPreferenceClickListener(preference -> {
                if ("whitelist_domain_1_button".equals(buttonKey) || "whitelist_domain_2_button".equals(buttonKey)) {
                    // Copy text to clipboard for whitelist buttons
                    ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    CharSequence copiedText = getString(textResource);
                    ClipData copiedData = ClipData.newPlainText("text", copiedText);
                    clipboardManager.setPrimaryClip(copiedData);
                    Toast.makeText(getContext(), "Text copied!", Toast.LENGTH_SHORT).show();
                } else {
                    // Open URL in browser for other buttons
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(textResource)));
                    startActivity(intent);
                }
                return true;
            });
        }

        // Set up click listener for a button preference that starts an intent
        private void setupButtonForIntent(String buttonKey) {
            Preference button = findPreference(buttonKey);
            assert button != null;
            button.setOnPreferenceClickListener(preference -> {
                if ("author_button".equals(buttonKey)) {
                    Intent intent = new Intent(getContext(), AuthorActivity.class);
                    startActivity(intent);
                }
                if ("permission_button".equals(buttonKey)) {
                    Intent intent = new Intent(getContext(), PermissionActivity.class);
                    startActivity(intent);
                }
                return true;
            });
        }

        // Set up listener for dark mode preference changes
        private void setupDarkModeChangeListener(ListPreference setting, SharedPreferences sharedPreferences) {
            setting.setOnPreferenceChangeListener((preference, newValue) -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    // Store new value in SharedPreferences
                    sharedPreferences.edit().putString("dark_mode", newValue.toString()).apply();
                    // Restart activity to apply changes
                    ProcessPhoenix.triggerRebirth(requireContext());
                }
                return true;
            });
        }

        // Set visibility of a preference
        private void setPreferenceVisibility(String key, Boolean visibility) {
            Preference preference = findPreference(key);
            if (preference != null) {
                preference.setVisible(visibility);
            }
        }
    }

    // Inflate menu for the activity
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_back_only, menu);
        return true;
    }

    // Handle menu item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.back) {
            // Navigate back to MainActivity
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
        }
        return super.onContextItemSelected(item);
    }
}
