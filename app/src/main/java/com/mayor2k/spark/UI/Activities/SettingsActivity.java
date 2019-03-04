package com.mayor2k.spark.UI.Activities;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;

import com.jaeger.library.StatusBarUtil;
import com.mayor2k.spark.R;

public class SettingsActivity extends AppCompatPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setLightMode(SettingsActivity.this);
        setContentView(R.layout.activity_setting);
        addPreferencesFromResource(R.xml.preferences);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_24dp_black));
        toolbar.setNavigationOnClickListener(v -> finish());

        PreferenceCategory preferenceCategory = (PreferenceCategory)findPreference("pref_key_storage_settings");
        SwitchPreference switchPreference = (SwitchPreference)findPreference("notifications_style");
        SwitchPreference colorPreference = (SwitchPreference)findPreference("notifications_color");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N )
            preferenceCategory.removePreference(switchPreference);
        else if (switchPreference.isChecked())
            colorPreference.setEnabled(false);
        switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (!switchPreference.isChecked()){
                colorPreference.setChecked(false);
                colorPreference.setEnabled(false);
            }else{
                colorPreference.setEnabled(true);
            }
            return true;
        });
    }
}
