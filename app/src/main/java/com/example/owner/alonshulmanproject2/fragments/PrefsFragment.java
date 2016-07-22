package com.example.owner.alonshulmanproject2.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.example.owner.alonshulmanproject2.R;

/**
 * Created by Owner on 30/03/2016.
 */
public class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private ListPreference unit, mode;
    public static final String UNITS = "units";
    public static final String TRANSPORT_MODE = "transport_mode";
    public static final String UNIT_KM = "1";
    public static final String UNIT_MILES = "2";
    public static final String TRANSPORT_MODE_DRIVING = "1";
    public static final String TRANSPORT_MODE_WALKING = "2";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_screen);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        unit = (ListPreference) findPreference(UNITS);
        mode = (ListPreference) findPreference(TRANSPORT_MODE);

        String currentUnit = preferences.getString(UNITS, UNIT_KM);
        if(currentUnit.equals(UNIT_KM)){
            unit.setSummary(getActivity().getResources().getString(R.string.km));
            unit.setValue(UNIT_KM);
        } else{
            unit.setSummary(getActivity().getResources().getString(R.string.miles));
            unit.setValue(UNIT_MILES);
        }
        unit.setOnPreferenceChangeListener(this);

        String currentTransportMode = preferences.getString(TRANSPORT_MODE,TRANSPORT_MODE_DRIVING);
        if(currentTransportMode.equals(TRANSPORT_MODE_DRIVING)){
            mode.setSummary(getActivity().getResources().getString(R.string.driving));
            mode.setValue(TRANSPORT_MODE_DRIVING);
        } else{
            mode.setSummary(getActivity().getResources().getString(R.string.walking));
            mode.setValue(TRANSPORT_MODE_WALKING);
        }
        mode.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()){
            case UNITS:
                if(newValue.toString().equals(UNIT_KM)){
                    unit.setSummary(getActivity().getResources().getString(R.string.km));
                } else{
                    unit.setSummary(getActivity().getResources().getString(R.string.miles));
                }
                break;
            case TRANSPORT_MODE:
                if(newValue.toString().equals(TRANSPORT_MODE_DRIVING)){
                    mode.setSummary(getActivity().getResources().getString(R.string.driving));
                } else{
                    mode.setSummary(getActivity().getResources().getString(R.string.walking));
                }
                break;
        }
        return true;
    }
}
