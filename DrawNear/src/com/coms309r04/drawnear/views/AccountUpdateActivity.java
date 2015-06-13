package com.coms309r04.drawnear.views;



import com.coms309r04.drawnear.R;
import com.parse.ParseUser;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class AccountUpdateActivity extends PreferenceActivity {
	
	private EditTextPreference update_email;
	private EditTextPreference update_password;


@Override
protected void onCreate(final Bundle savedInstanceState)
{
    super.onCreate(savedInstanceState);
    getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    update_email = (EditTextPreference) findPreference("email");
    update_password = (EditTextPreference) findPreference("password");
};

public static class MyPreferenceFragment extends PreferenceFragment
{
	public MyPreferenceFragment() {
	}
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
    
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.acc);
       
    }
}
}