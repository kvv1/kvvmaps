package com.smartbean.androidutils.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;

import com.smartbean.androidutils.util.Drawables;

public abstract class FragmentActivityTabs extends ActionBarActivity {

	protected abstract ViewPager getPager();

	private List<String> titles = new ArrayList<String>();
	private List<Fragment> fragments = new ArrayList<Fragment>();

	public void add(String title, Fragment fragment) {
		titles.add(title);
		fragments.add(fragment);
		setPager();
	}

	public void selectTab(int tab) {
		getPager().setCurrentItem(tab, true);
	}

	private void setPager() {
		getPager().setAdapter(
				new FragmentPagerAdapter(getSupportFragmentManager()) {
					@Override
					public Fragment getItem(int position) {
						return fragments.get(position);
					}

					@Override
					public int getCount() {
						return fragments.size();
					}

					@Override
					public CharSequence getPageTitle(int position) {
						return titles.get(position);
					}
				});

		final ActionBar actionBar = getSupportActionBar();
		actionBar.removeAllTabs();

		// For each of the sections in the app, add a tab to the action bar.
		for (String title : titles) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab().setText(title)
					.setTabListener(tabListener));
		}

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		getPager().setOnPageChangeListener(
				new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// setContentView(getLayoutId());

		// Set up the ViewPager with the sections adapter.
		// mViewPager = (ViewPager) findViewById(getPagerId());

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Drawables.unbindDrawables(getPager());
	}

	ActionBar.TabListener tabListener = new TabListener() {
		@Override
		public void onTabSelected(ActionBar.Tab tab,
				FragmentTransaction fragmentTransaction) {
			getPager().setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(ActionBar.Tab tab,
				FragmentTransaction fragmentTransaction) {
		}

		@Override
		public void onTabReselected(ActionBar.Tab tab,
				FragmentTransaction fragmentTransaction) {
		}
	};

}
