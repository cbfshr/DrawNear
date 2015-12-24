package com.coms309r04.drawnear.tools;

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;

public class TabListener<T extends Activity> implements ActionBar.TabListener {
	public static boolean tabsActive = false;

	private Class<T> mActivityTo;
	private Context mActivityFrom;

	/**
	 * Constructor used each time a new tab is created.
	 * 
	 * @param activity
	 *            The host Activity, used to instantiate the fragment
	 * @param tag
	 *            The identifier tag for the fragment
	 * @param clz
	 *            The fragment's Class, used to instantiate the fragment
	 */
	public TabListener(Context from, Class<T> to) {
		this.mActivityTo = to;
		this.mActivityFrom = from;

		tabsActive = false;
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if(tabsActive && (this.mActivityTo != this.mActivityFrom.getClass())) {
			Intent intent = new Intent(this.mActivityFrom, this.mActivityTo);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			this.mActivityFrom.startActivity(intent);
			//((Activity) this.mActivityFrom).overridePendingTransition(0, 0);
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
	}
}
