package com.coms309r04.drawnear.views;

import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.views.AccountUpdateActivity.MyPreferenceFragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class AccountActivity extends Activity {

	private SharedPreferences share;
	private OnSharedPreferenceChangeListener listener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account);
		MyPreferenceFragment frag = new MyPreferenceFragment();
		getFragmentManager().beginTransaction()
		.add(R.id.Mycontainer, frag)
		.commit();
		
		share = PreferenceManager.getDefaultSharedPreferences(this);
		//listener = new OnSharedPreferenceChangeListener() {

		//	@Override
			//public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
				//	String key) {
				//AccountActivity.this.modify();

		//	}
		//};
	//	share.registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.account, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}



	public void update(View v){
		Intent intent = new Intent(this, AccountUpdateActivity.class);
		startActivity(intent);

	}
	/*public void  modify(){
		String pref= share.getString("email", "enter your id");
		UserItem.displayText(AccountActivity.this, R.id.mail, pref);

		String pref1= share.getString("password", "enter your password");
		UserItem.displayText(AccountActivity.this, R.id.pass, pref1);

		String pref2= share.getString("u-name", "enter your Username");
		UserItem.displayText(AccountActivity.this, R.id.user_name, pref2);


	}*/


}
