package zorro.dimyon.calleridentity.activities;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import zorro.dimyon.calleridentity.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            SwitchPreferenceCompat blockAllSpammers = findPreference("block_all_spammers");
            SwitchPreferenceCompat blockTopSpammers = findPreference("block_top_spammers");

            if (blockAllSpammers != null && blockTopSpammers != null) {
                // Initialize the state when the preferences are first loaded
                updateBlockTopSpammersState(blockAllSpammers.isChecked(), blockTopSpammers);

                blockAllSpammers.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isEnabled = (boolean) newValue;
                    updateBlockTopSpammersState(isEnabled, blockTopSpammers);
                    return true; // Allow the preference to be changed
                });

                blockTopSpammers.setOnPreferenceChangeListener((preference, newValue) -> {
                    return !blockAllSpammers.isChecked(); // Prevent changing if block_all_spammers is enabled
                });
            }
        }
    }

    private static void updateBlockTopSpammersState(boolean isBlockAllEnabled, SwitchPreferenceCompat blockTopSpammers) {
        blockTopSpammers.setEnabled(!isBlockAllEnabled);
        if (isBlockAllEnabled) {
            blockTopSpammers.setChecked(true);
        }
    }
}