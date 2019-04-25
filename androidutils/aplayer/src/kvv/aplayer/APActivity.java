package kvv.aplayer;

import com.smartbean.androidutils.activity.FragmentActivityTabsNoActionBar;
import com.smartbean.androidutils.service.ServiceConnectionAdapter;

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
import kvv.aplayer.files.FilesSectionFragmentList;
import kvv.aplayer.files.FilesSectionFragmentTape;
import kvv.aplayer.files.TextSectionFragment;
import kvv.aplayer.folders.FoldersSectionFragment;
import kvv.aplayer.player.Player.PlayerAdapter;
import kvv.aplayer.player.Player.PlayerListener;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.IAPService;

@SuppressLint("NewApi")
public class APActivity extends FragmentActivityTabsNoActionBar {

	public static final int BUTTONS_DELAY = 3000;

	private static final int RESULT_SETTINGS = 1;

	@Override
	protected ViewPager getPager() {
		return (ViewPager) findViewById(R.id.pager);
	}

	private PlayerListener listener = new PlayerAdapter() {
		@Override
		public void fileChanged() {
			updateWakeLock();
		}
	};

	public void selectMainPage() {
		selectTab(1);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println(getClass().getSimpleName() + ".onCreate()");

		super.onCreate(null);
		setContentView(R.layout.activity_ap);

		// File path = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
		// System.out.println("+++++++++++++++" + path);
		// path =
		// Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
		// System.out.println("+++++++++++++++" + path);
		//
		//
		//
		// FileWriter wr;
		// try {
		// wr = new FileWriter(new File(path, "text.txt"));
		//
		// wr.write("xaxa");
		//
		//
		// wr.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		//

		startService(new Intent(this, APService.class));
		bindService(new Intent(this, APService.class), conn,
				Context.BIND_AUTO_CREATE);

		// if (savedInstanceState == null) {
		add("Text", new TextSectionFragment());
		add("Files", new FilesSectionFragmentList());
		add("Files1", new FilesSectionFragmentTape());
		add("Folders", new FoldersSectionFragment());
		selectMainPage();
		// }
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
		int itemId = item.getItemId();
		if (itemId == R.id.action_exit) {
			exit();
			return true;
		} else if (itemId == R.id.action_reload) {
			if (conn.service != null)
				conn.service.reload();
			return true;
		} else if (itemId == R.id.action_settings) {
			Intent i = new Intent(this, Preferences.class);
			startActivityForResult(i, RESULT_SETTINGS);
			return true;
		} else {
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
				conn.service.settingsChanged();
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
