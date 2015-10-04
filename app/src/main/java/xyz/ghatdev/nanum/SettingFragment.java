package xyz.ghatdev.nanum;

import android.os.Bundle;
import android.preference.PreferenceFragment;


public class SettingFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.perference);
    }
}
