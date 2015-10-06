package kvv.aplayer;

import kvv.aplayer.chart.ChartsFragment;
import kvv.aplayer.files.FilesSectionFragmentList;
import kvv.aplayer.folders.FoldersSectionFragment;
import kvv.aplayer.player.Player.OnChangedHint;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.APServiceListener;
import kvv.aplayer.service.APServiceListenerAdapter;
import kvv.aplayer.service.IAPService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.smartbean.androidutils.activity.FragmentActivityTabsNoActionBar;
import com.smartbean.androidutils.service.ServiceConnectionAdapter;

public class APActivity extends FragmentActivityTabsNoActionBar {

	public static final int BUTTONS_DELAY = 3000;

	private static final int RESULT_SETTINGS = 1;

	@Override
	protected ViewPager getPager() {
		return (ViewPager) findViewById(R.id.pager);
	}
/*
	@Override
	protected int getLayoutId() {
		return R.layout.activity_ap;
	}

	@Override
	protected int getCount() {
		return 2;
	}

	@Override
	protected Fragment getItem(int position) {
		switch (position) {
		case 0:
			return new FilesSectionFragmentList();
		case 1:
			return new FoldersSectionFragment();
		case 2:
			return new ChartsFragment();
		}
		return null;

	}

	@Override
	protected CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return "Files";
		case 1:
			return "Folders";
		case 2:
			return "Charts";
		}
		return null;
	}
*/
	private APServiceListener listener = new APServiceListenerAdapter() {
		@Override
		public void onChanged(OnChangedHint hint) {
			updateWakeLock();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ap);
		
		startService(new Intent(this, APService.class));
		bindService(new Intent(this, APService.class), conn,
				Context.BIND_AUTO_CREATE);

		add("Files", new FilesSectionFragmentList());
		add("Folders", new FoldersSectionFragment());
		add("Charts", new ChartsFragment());

	}

	@Override
	protected void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(listener);
		unbindService(conn);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.a, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// try {
		// menu.findItem(R.id.action_update).setTitle(
		// "Ver. "
		// + getPackageManager().getPackageInfo(
		// this.getPackageName(), 0).versionName);
		// } catch (NameNotFoundException e) {
		// }
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_exit:
			exit();
			return true;
		case R.id.action_reload:
			if (conn.service != null)
				conn.service.reload();
			return true;
		case R.id.action_settings:
			Intent i = new Intent(this, Preferences.class);
			startActivityForResult(i, RESULT_SETTINGS);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			_onBG();
			_onFG();
			break;
		}

	}

	private void exit() {
		Intent i = new Intent(this, APService.class);
		stopService(i);
		finish();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				System.exit(0);
			}
		}, 5000);
	}

	private ServiceConnectionAdapter<IAPService> conn = new ServiceConnectionAdapter<IAPService>() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			super.onServiceConnected(name, binder);
			service.addListener(listener);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		_onFG();
	}

	@Override
	protected void onPause() {
		_onBG();
		super.onPause();
		System.gc();
	}

	private PowerManager.WakeLock wakeLock;

	boolean fg;

	private void lock() {
		if (wakeLock == null) {
			System.out.println("LOCK");
			wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
					.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
							"kvvMaps wake lock");
			wakeLock.acquire();
		}
	}

	private void unlock() {
		if (wakeLock != null) {
			System.out.println("UNLOCK");
			wakeLock.release();
			wakeLock = null;
		}
	}

	void updateWakeLock() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean navMode = settings.getBoolean(
				getString(R.string.prefNavigatorMode), false);

		boolean playing = conn.service != null && conn.service.isPlaying();

		System.out.println("updateWakeLock playing=" + playing + " fg=" + fg);

		if (navMode) {
			if (fg && playing)
				lock();
			else
				unlock();
		} else {
			unlock();
		}
	}

	public void _onFG() {
		fg = true;
		updateWakeLock();
	}

	public void _onBG() {
		fg = false;
		updateWakeLock();
	}

	public void selectMainPage() {
		selectTab(0);
	}

}
