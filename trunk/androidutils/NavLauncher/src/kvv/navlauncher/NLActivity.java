package kvv.navlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class NLActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nl);

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

		((Button) findViewById(R.id.yamaps))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent i = getPackageManager()
								.getLaunchIntentForPackage(
										"ru.yandex.yandexmaps");
						i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(i);
						finish();
					}
				});

		((Button) findViewById(R.id.gmaps))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(
								android.content.Intent.ACTION_VIEW, Uri
										.parse("http://maps.google.com/maps"));
						startActivity(intent);
						finish();
					}
				});

		((Button) findViewById(R.id.kvvmaps))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent i = getPackageManager()
								.getLaunchIntentForPackage("kvv.kvvmap");
						i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(i);
						finish();
					}
				});

		((Button) findViewById(R.id.pause))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						sendBroadcast(new Intent()
								.setAction("kvv.aplayer.PLAY_PAUSE"));
						finish();
					}
				});

		((Button) findViewById(R.id.prev))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						sendBroadcast(new Intent()
								.setAction("kvv.aplayer.PREV"));
						finish();
					}
				});

		((Button) findViewById(R.id.next))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						sendBroadcast(new Intent()
								.setAction("kvv.aplayer.NEXT"));
						finish();
					}
				});

		((Button) findViewById(R.id.brightness_max))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						WindowManager.LayoutParams localLayoutParams = getWindow()
								.getAttributes();
						localLayoutParams.screenBrightness = 1;
						getWindow().setAttributes(localLayoutParams);

						Settings.System
								.putInt(getContentResolver(),
										"screen_brightness",
										(int) (localLayoutParams.screenBrightness * 255));
						finish();
					}
				});

		((Button) findViewById(R.id.brightness_min))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						WindowManager.LayoutParams localLayoutParams = getWindow()
								.getAttributes();
						localLayoutParams.screenBrightness = 0.2F;
						getWindow().setAttributes(localLayoutParams);

						Settings.System
								.putInt(getContentResolver(),
										"screen_brightness",
										(int) (localLayoutParams.screenBrightness * 255));
						finish();
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
		// TODO Auto-generated method stub
		return super.onPrepareOptionsMenu(menu);
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
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(i);
			return true;
		case R.id.action_update:
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://palermo.ru/vladimir/NavLauncher.apk")));
			return true;
		}

		// TODO Auto-generated method stub
		return super.onMenuItemSelected(featureId, item);
	}
}
