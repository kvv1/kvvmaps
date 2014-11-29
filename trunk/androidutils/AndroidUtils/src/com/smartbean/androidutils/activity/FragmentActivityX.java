package com.smartbean.androidutils.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.smartbean.androidutils.util.Drawables;

public abstract class FragmentActivityX extends FragmentActivity {
	protected ViewPager mViewPager;

	protected abstract int getLayoutId();

	protected abstract int getPagerId();

	protected abstract Fragment getItem(int arg0);

	protected abstract int getCount();

	protected abstract CharSequence getPageTitle(int position);

	protected void onBG() {
	}

	protected void onFG() {
	}

	@Override
	protected void onResume() {
		super.onResume();
		onFG();
	}

	// @Override
	// public void onWindowFocusChanged(boolean hasFocus) {
	// System.out.println("onWindowFocusChanged " + hasFocus);
	// if (hasFocus)
	// onFG();
	// else
	// onBG();
	//
	// super.onWindowFocusChanged(hasFocus);
	// }

	@Override
	protected void onPause() {
		onBG();
		super.onPause();
		System.gc();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutId());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(getPagerId());
		mViewPager.setAdapter(new FragmentPagerAdapter(
				getSupportFragmentManager()) {
			@Override
			public Fragment getItem(int arg0) {
				return FragmentActivityX.this.getItem(arg0);
			}

			@Override
			public int getCount() {
				return FragmentActivityX.this.getCount();
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return FragmentActivityX.this.getPageTitle(position);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Drawables.unbindDrawables(mViewPager);
	}
}
