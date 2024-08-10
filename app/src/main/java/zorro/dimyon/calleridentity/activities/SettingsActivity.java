package zorro.dimyon.calleridentity.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.appbar.MaterialToolbar;

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

//        ------------------------------------------------------------------------------------------
        MaterialToolbar toolbar = findViewById(R.id.toolbar_include);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.settings));
        }

        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.arrow_back_24));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
//        ------------------------------------------------------------------------------------------
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // Setting up dependencies between preferences
            setupDependency("block_all_spammers", "block_top_spammers");
            setupDependency("reject_all_incoming_calls", "reject_unknown_incoming_calls");
        }

        /**
         * Sets up a dependency where enabling the master preference automatically enables
         * and locks the dependent preference.
         *
         * @param masterKey    The key of the master SwitchPreferenceCompat.
         * @param dependentKey The key of the dependent SwitchPreferenceCompat.
         */
        private void setupDependency(String masterKey, String dependentKey) {
            SwitchPreferenceCompat masterPreference = findPreference(masterKey);
            SwitchPreferenceCompat dependentPreference = findPreference(dependentKey);

            if (masterPreference != null && dependentPreference != null) {
                // Initialize the dependent preference state based on the master preference
                updateDependentState(masterPreference.isChecked(), dependentPreference);

                // Listener for changes in the master preference
                masterPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isEnabled = (Boolean) newValue;
                    updateDependentState(isEnabled, dependentPreference);
                    return true; // Allow the master preference state change
                });

                // Listener for attempts to change the dependent preference
                dependentPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    // If master is enabled, prevent changes to dependent
                    return !masterPreference.isChecked();
                });
            }
        }

        /**
         * Updates the state of the dependent preference based on the master preference's state.
         *
         * @param isMasterEnabled    The current state of the master preference.
         * @param dependentPreference The dependent SwitchPreferenceCompat to update.
         */
        private void updateDependentState(boolean isMasterEnabled, SwitchPreferenceCompat dependentPreference) {
            dependentPreference.setChecked(isMasterEnabled || dependentPreference.isChecked());
            dependentPreference.setEnabled(!isMasterEnabled);
        }
    }
}
