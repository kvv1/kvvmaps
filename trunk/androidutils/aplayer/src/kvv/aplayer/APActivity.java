package kvv.aplayer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.smartbean.androidutils.activity.FragmentActivityX;
import com.smartbean.androidutils.service.ServiceConnectionAdapter;

public class APActivity extends FragmentActivityX {
	@Override
	protected int getLayoutId() {
		return R.layout.activity_ap;
	}

	@Override
	protected int getPagerId() {
		return R.id.pager;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(this, APService.class));
		bindService(new Intent(this, APService.class), conn,
				Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onDestroy() {
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
		try {
			menu.findItem(R.id.action_update).setTitle(
					"Ver. "
							+ getPackageManager().getPackageInfo(
									this.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
		}
		// TODO Auto-generated method stub
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_exit:
			exit();
			return true;
		case R.id.action_bookmark:
			if (conn.service != null)
				conn.service.addBookmark();
			return true;
		case R.id.action_update:
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://palermo.ru/vladimir/aplayer.apk")));
			return true;
		default:
			return super.onOptionsItemSelected(item);
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

	@Override
	protected Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a DummySectionFragment (defined as a static inner class
		// below) with the page number as its lone argument.

		switch (position) {
		case 0:
			return new FilesSectionFragment();
		case 1:
			return new FoldersSectionFragment();
		case 2:
			return new BookmarksSectionFragment();
		}
		return null;

	}

	@Override
	protected int getCount() {
		return 3;
	}

	@Override
	protected CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return "Files";
		case 1:
			return "Folders";
		case 2:
			return "Bookmarks";
		}
		return null;
	}

	private ServiceConnectionAdapter<IAPService> conn = new ServiceConnectionAdapter<IAPService>() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			super.onServiceConnected(name, binder);
			if (service.getFolders().size() == 0) {
				new AlertDialog.Builder(APActivity.this)
						.setMessage(
								"No folders in '"
										+ APService.ROOT.getAbsolutePath()
										+ "'")
						.setOnCancelListener(new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								exit();
							}
						}).show();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			super.onServiceDisconnected(name);
		}
	};

}
