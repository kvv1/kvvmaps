package com.smartbean.androidutils.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.smartbean.androidutils.util.Drawables;

public abstract class FragmentActivityTabsNoActionBar extends FragmentActivity {

	protected abstract ViewPager getPager();

	private List<String> titles = new ArrayList<String>();
	private List<Fragment> fragments = new ArrayList<Fragment>();

	public void add(String title, Fragment fragment) {
		titles.add(title);
		fragments.add(fragment);
		setPager();
	}

	private void setPager() {
		getPager().setAdapter(new FragmentPagerAdapter(
				getSupportFragmentManager()) {
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
	}
	
	public void selectTab(int tab) {
		getPager().setCurrentItem(tab, true);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Drawables.unbindDrawables(getPager());
	}
}
