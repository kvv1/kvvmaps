package kvv.navlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.smartbean.androidutils.util.AsyncCallback;
import com.smartbean.androidutils.util.Utils;

public class NLActivity extends Activity {

	class Navigator {
		String name;
		int imageId;
		Intent intent;

		public Navigator(String name, int imageId, Intent intent) {
			this.name = name;
			this.imageId = imageId;
			this.intent = intent;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Navigator[] navigators;
	private Navigator currentNavigator;

	private void updateNavUI() {
		Button b = (Button) findViewById(R.id.navigator);
		b.setText(currentNavigator.name);
		b.setCompoundDrawablesWithIntrinsicBounds(0, currentNavigator.imageId,
				0, 0);
	}

	private Handler handler = new Handler();

	private Runnable finishTimer = new Runnable() {
		@Override
		public void run() {
			finish();
		}
	};

	private void startFinishTimer() {
		handler.removeCallbacks(finishTimer);
		handler.postDelayed(finishTimer, 2000);
	}

//	private void cancelFinishTimer() {
//		handler.removeCallbacks(finishTimer);
//	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nl);

		navigators = new Navigator[] {
				new Navigator("Яндекс карты", R.drawable.yamap,
						getPackageManager().getLaunchIntentForPackage(
								"ru.yandex.yandexmaps")),
				new Navigator("Яндекс навигатор", R.drawable.yanavi,
						getPackageManager().getLaunchIntentForPackage(
								"ru.yandex.yandexnavi")),
				new Navigator("Карты Google", R.drawable.gmaps, new Intent(
						android.content.Intent.ACTION_VIEW,
						Uri.parse("http://maps.google.com/maps"))),
				new Navigator("KVV maps", R.drawable.kvvmaps,
						getPackageManager().getLaunchIntentForPackage(
								"kvv.kvvmap")), };

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		int navIdx = settings.getInt("navigator", 0);
		if (navIdx >= navigators.length)
			navIdx = 0;

		currentNavigator = navigators[navIdx];
		updateNavUI();

		((Button) findViewById(R.id.aplayer))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent i = getPackageManager()
								.getLaunchIntentForPackage("kvv.aplayer");
						i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(i);
						finish();
					}
				});

		((Button) findViewById(R.id.navigator))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent i = currentNavigator.intent;
						i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(i);
						finish();
					}
				});

		((Button) findViewById(R.id.navigator))
				.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View arg0) {
						Utils.select(NLActivity.this, "Выберите навигатор",
								Utils.toStringArray(navigators, null),
								new AsyncCallback<Integer>() {
									@Override
									public void onSuccess(Integer res) {
										currentNavigator = navigators[res];
										updateNavUI();
										SharedPreferences settings = PreferenceManager
												.getDefaultSharedPreferences(NLActivity.this);
										SharedPreferences.Editor editor = settings
												.edit();
										editor.putInt("navigator", res);
										editor.commit();
									}
								});
						return false;
					}
				});

		((Button) findViewById(R.id.pause))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						sendBroadcast(new Intent()
								.setAction("kvv.aplayer.PLAY_PAUSE"));
						startFinishTimer();
					}
				});

		((Button) findViewById(R.id.prev))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						sendBroadcast(new Intent()
								.setAction("kvv.aplayer.PREV"));
						startFinishTimer();
					}
				});

		((Button) findViewById(R.id.next))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						sendBroadcast(new Intent()
								.setAction("kvv.aplayer.NEXT"));
						startFinishTimer();
					}
				});

		((Button) findViewById(R.id.brightness))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						WindowManager.LayoutParams localLayoutParams = getWindow()
								.getAttributes();

						int br = Settings.System.getInt(getContentResolver(),
								"screen_brightness", 0);

						System.out.println("BR " + br);

						if (br > 250) {
							localLayoutParams.screenBrightness = 0.2f;
							Settings.System
									.putInt(getContentResolver(),
											"screen_brightness",
											(int) (localLayoutParams.screenBrightness * 255));
						} else {
							localLayoutParams.screenBrightness = 1;
							Settings.System
									.putInt(getContentResolver(),
											"screen_brightness",
											(int) (localLayoutParams.screenBrightness * 255));
						}

						getWindow().setAttributes(localLayoutParams);
						startFinishTimer();
					}
				});

		((Button) findViewById(R.id.off))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						sendBroadcast(new Intent()
								.setAction("kvv.aplayer.PAUSE"));
					}
				});

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
			menu.findItem(R.id.action_update).setTitle(
					"Ver. "
							+ getPackageManager().getPackageInfo(
									this.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
		}
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent i = getPackageManager().getLaunchIntentForPackage(
					"com.android.settings");
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(i);
			return true;
		case R.id.action_default_launcher:
			getPackageManager().clearPackagePreferredActivities(
					getPackageName());
			final Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			return true;
		case R.id.action_camera:
			i = getPackageManager().getLaunchIntentForPackage(
					"com.sec.android.app.camera");
			i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(i);
			return true;
		case R.id.action_update:
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://palermo.ru/vladimir/NavLauncher.apk")));
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
}
