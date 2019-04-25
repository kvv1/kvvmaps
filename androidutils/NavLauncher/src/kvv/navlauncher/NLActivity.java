package kvv.navlauncher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.smartbean.androidutils.util.AsyncCallback;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

public class NLActivity extends Activity {

	private int[] buttons = { R.id.app0, R.id.app1, R.id.app2, R.id.app3, R.id.app4, R.id.app5, R.id.app6, R.id.app7,
			R.id.app8, R.id.app9 };
	private AppInfo[] appInfos = new AppInfo[buttons.length];

	private Handler handler = new Handler();

	private Runnable finishTimer = new Runnable() {
		@Override
		public void run() {

			System.out.println("******************************");
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			for (RunningTaskInfo t : am.getRunningTasks(10)) {
				String packageName = t.topActivity.getPackageName();
				String className = t.topActivity.getClassName();
				System.out.println(className);
			}
			System.out.println("******************************");

			List<RunningTaskInfo> runningTasks = am.getRunningTasks(10);
			if (runningTasks.size() > 1) {
				RunningTaskInfo info = runningTasks.get(1);
				String packageName = info.topActivity.getPackageName();
				String className = info.topActivity.getClassName();

				try {
					Intent i = new Intent();
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
					/* | Intent.FLAG_ACTIVITY_CLEAR_TOP */);
					i.setComponent(new ComponentName(packageName, className));
					startActivity(i);
					// finish();
					return;
				} catch (Exception e) {
					Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				}
			}
		}
		// finish();
	};

	private void startFinishTimer() {
		handler.removeCallbacks(finishTimer);
		handler.postDelayed(finishTimer, 2000);
	}

	private void updateAppButtons() {
		List<AppInfo> infos = getInstalledComponentList();

		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		for (int button = 0; button < buttons.length; button++) {
			final int button1 = button;

			Button b = (Button) findViewById(buttons[button]);
			b.setText("?");
			b.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

			String pack = settings.getString("appInfo" + button, null);

			for (AppInfo appInfo : infos) {
				if (appInfo.packageName.equals(pack)) {
					appInfos[button] = appInfo;
					b.setText(appInfo.appName);
					b.setCompoundDrawablesWithIntrinsicBounds(null, appInfo.icon, null, null);
				}
			}

			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					AppInfo appInfo = appInfos[button1];

					if (appInfo == null)
						return;

					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
					for (RunningTaskInfo t : am.getRunningTasks(10)) {
						String packageName = t.topActivity.getPackageName();
						String className = t.topActivity.getClassName();
						System.out.println(packageName + " " + className);

						try {
							if (packageName.equals(appInfo.packageName)) {
								Intent i = new Intent();
								i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
								/* | Intent.FLAG_ACTIVITY_CLEAR_TOP */);
								i.setComponent(new ComponentName(packageName, className));
								startActivity(i);
								// finish();
								return;
							}
						} catch (Exception e) {
						}
					}

					Intent i = getPackageManager().getLaunchIntentForPackage(appInfo.packageName);

					System.out.println("*** " + appInfo.packageName);

					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
					// finish();
				}
			});

			b.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					selectApp(new AsyncCallback<AppInfo>() {
						@Override
						public void onSuccess(AppInfo res) {
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("appInfo" + button1, res.packageName);
							editor.commit();
							updateAppButtons();
						}
					});
					return false;
				}
			});

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nl);

		updateAppButtons();

		((Button) findViewById(R.id.pause)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getApplication().sendBroadcast(new Intent().setAction(Intent.ACTION_MEDIA_BUTTON).putExtra(
						Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)));
				startFinishTimer();
			}
		});

		((Button) findViewById(R.id.prev)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getApplication().sendBroadcast(new Intent().setAction(Intent.ACTION_MEDIA_BUTTON).putExtra(
						Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS)));
				startFinishTimer();
			}
		});

		((Button) findViewById(R.id.next)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getApplication().sendBroadcast(new Intent().setAction(Intent.ACTION_MEDIA_BUTTON).putExtra(
						Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)));
				startFinishTimer();
			}
		});

//		((Button) findViewById(R.id.brightness)).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
//
//				int br = Settings.System.getInt(getContentResolver(), "screen_brightness", 0);
//
//				System.out.println("BR " + br);
//
//				if (br > 250) {
//					localLayoutParams.screenBrightness = 0.1f;
//					Settings.System.putInt(getContentResolver(), "screen_brightness",
//							(int) (localLayoutParams.screenBrightness * 255));
//				} else {
//					localLayoutParams.screenBrightness = 1;
//					Settings.System.putInt(getContentResolver(), "screen_brightness",
//							(int) (localLayoutParams.screenBrightness * 255));
//				}
//
//				getWindow().setAttributes(localLayoutParams);
//				startFinishTimer();
//			}
//		});
//
//		((Button) findViewById(R.id.off)).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				getApplication().sendBroadcast(new Intent().setAction(Intent.ACTION_MEDIA_BUTTON).putExtra(
//						Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)));
//			}
//		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nl, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		try {
			menu.findItem(R.id.action_update)
					.setTitle("Ver. " + getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
		}
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent i = getPackageManager().getLaunchIntentForPackage("com.android.settings");
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(i);
			return true;
		case R.id.action_default_launcher:
			getPackageManager().clearPackagePreferredActivities(getPackageName());
			final Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			return true;
		case R.id.action_camera:
			i = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.camera");
			i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(i);
			return true;
		case R.id.action_update:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://palermo.ru/vladimir/NavLauncher.apk")));
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	static class AppInfo {

		public String appName;
		public String packageName;
		public Drawable icon;

		public AppInfo(String appName, String packageName, Drawable icon) {
			super();
			this.appName = appName;
			this.packageName = packageName;
			this.icon = icon;
		}

	}

	private void selectApp(final AsyncCallback<AppInfo> callback) {
		final List<AppInfo> list = getInstalledComponentList();

		Collections.sort(list, new Comparator<AppInfo>() {
			@Override
			public int compare(AppInfo lhs, AppInfo rhs) {
				return lhs.appName.compareTo(rhs.appName);
			}
		});

		ListAdapter adapter = new ArrayAdapter<AppInfo>(NLActivity.this, android.R.layout.select_dialog_item,
				android.R.id.text1, list) {
			public View getView(int position, View convertView, ViewGroup parent) {
				// Use super class to create the View
				View v = super.getView(position, convertView, parent);
				TextView tv = (TextView) v.findViewById(android.R.id.text1);

				// Put the image on the TextView
				tv.setCompoundDrawablesWithIntrinsicBounds(list.get(position).icon, null, null, null);

				// Add margin between image and text (support
				// various screen densities)
				int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
				tv.setCompoundDrawablePadding(dp5);

				tv.setText(list.get(position).appName);

				return v;
			}
		};

		new AlertDialog.Builder(NLActivity.this).setTitle("Select Appliction")
				.setAdapter(adapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						callback.onSuccess(list.get(item));
					}
				}).show();
	}

	private List<AppInfo> getInstalledComponentList() {
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> ril = getPackageManager().queryIntentActivities(mainIntent, 0);
		List<AppInfo> componentList = new ArrayList<AppInfo>();

		for (ResolveInfo ri : ril) {
			if (ri.activityInfo != null) {
				String name = null;

				Resources res;
				try {
					res = getPackageManager().getResourcesForApplication(ri.activityInfo.applicationInfo);
					if (ri.activityInfo.labelRes != 0) {
						name = res.getString(ri.activityInfo.labelRes);
					} else {
						name = ri.activityInfo.applicationInfo.loadLabel(getPackageManager()).toString();
					}

					AppInfo appInfo = new AppInfo(name, ri.activityInfo.packageName,
							ri.activityInfo.loadIcon(getPackageManager()));

					componentList.add(appInfo);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return componentList;
	}
}
