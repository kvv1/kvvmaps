package com.smartbean.androidutils.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public abstract class ActivityX extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	}

	public void addTab(String title, Fragment fragment) {
		ActionBar actionBar = getActionBar();
		Tab tab1 = actionBar.newTab().setText(title);
		tab1.setTabListener(new TabListener(fragment));
		actionBar.addTab(tab1);
	}

	protected abstract int getId();
	
	class TabListener implements ActionBar.TabListener {

		private Fragment fragment;

		// The contructor.
		public TabListener(Fragment fragment) {
			this.fragment = fragment;
		}

		// When a tab is tapped, the FragmentTransaction replaces
		// the content of our main layout with the specified fragment;
		// that's why we declared an id for the main layout.
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(getId(), fragment);
		}

		// When a tab is unselected, we have to hide it from the user's view.
		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(fragment);
		}

		// Nothing special here. Fragments already did the job.
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {

		}
	}
}
