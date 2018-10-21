package com.mayor2k.spark.UI.Activities;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;

import com.mayor2k.spark.R;

public class SettingsActivity extends AppCompatPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
        setupActionBar();
    }
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N ){
                PreferenceCategory preferenceCategory = (PreferenceCategory)findPreference("pref_key_storage_settings");
                SwitchPreference switchPreference = (SwitchPreference)findPreference("notifications_style");
                preferenceCategory.removePreference(switchPreference);
            }
        }
    }
}