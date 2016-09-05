package kvv.aplayer;

import kvv.aplayer.files.FilesSectionFragment;
import kvv.aplayer.files.TextSectionFragment;
import kvv.aplayer.folders.FoldersSectionFragment;
import kvv.aplayer.player.Player.OnChangedHint;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.APServiceListener;
import kvv.aplayer.service.APServiceListenerAdapter;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
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
import android.view.Menu;
import android.view.MenuItem;

import com.smartbean.androidutils.activity.FragmentActivityTabsNoActionBar;
import com.smartbean.androidutils.service.ServiceConnectionAdapter;

@SuppressLint("NewApi")
public class APActivity extends FragmentActivityTabsNoActionBar {

	public static final int BUTTONS_DELAY = 3000;

	private static final int RESULT_SETTINGS = 1;

	@Override
	protected ViewPager getPager() {
		return (ViewPager) findViewById(R.id.pager);
	}

	private APServiceListener listener = new APServiceListenerAdapter() {
		@Override
		public void onChanged(OnChangedHint hint) {
			updateWakeLock();
		}
	};

	public void selectMainPage() {
		selectTab(1);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println(getClass().getSimpleName() + ".onCreate()");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ap);

		startService(new Intent(this, APService.class));
		bindService(new Intent(this, APService.class), conn,
				Context.BIND_AUTO_CREATE);

		add("Text", new TextSectionFragment());
		add("Files", new FilesSectionFragment());
		add("Folders", new FoldersSectionFragment());

		selectMainPage();
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

		System.out.println("onActivityResult()");

		switch (requestCode) {
		case RESULT_SETTINGS:
			updateWakeLock();

			if (conn.service != null)
				conn.service.modeChanged();

			// new Handler().postDelayed(new Runnable() {
			//
			// @Override
			// public void run() {
			// filesSectionFragmentList.setMagicEye();
			// }
			// }, 1000);

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
			updateWakeLock();
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		// View decorView = getWindow().getDecorView();
		// int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		// decorView.setSystemUiVisibility(uiOptions);

		// Remember that you should never show the action bar if the
		// status bar is hidden, so hide that too if necessary.
		// ActionBar actionBar = getActionBar();
		// actionBar.hide();

		fg = true;
		updateWakeLock();
	}

	@Override
	protected void onPause() {
		fg = false;
		updateWakeLock();
		super.onPause();
		System.gc();
	}

	private PowerManager.WakeLock wakeLock;

	private boolean fg;

	@SuppressWarnings("deprecation")
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

	private boolean isCarMode() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		return settings.getBoolean(getString(R.string.prefCarMode), false);
	}

	private void updateWakeLock() {
		boolean carMode = isCarMode();
		boolean playing = conn.service != null && conn.service.isPlaying();

		System.out.println("updateWakeLock playing=" + playing + " fg=" + fg);

		if (carMode && fg && playing)
			lock();
		else
			unlock();
	}

}
