package kvv.kvvmap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Comparator;

import com.smartbean.androidutils.util.Drawables;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.dlg.PathDlg;
import kvv.kvvmap.dlg.PlaceMarkDlg;
import kvv.kvvmap.maps.Maps;
import kvv.kvvmap.placemark.Path;
import kvv.kvvmap.placemark.PathSelection;
import kvv.kvvmap.service.IKvvMapsService;
import kvv.kvvmap.service.KvvMapsService;
import kvv.kvvmap.service.KvvMapsService.KvvMapsServiceListener;
import kvv.kvvmap.util.COLOR;
import kvv.kvvmap.util.ISelectable;
import kvv.kvvmap.util.InfoLevel;
import kvv.kvvmap.util.Utils;
import kvv.kvvmap.view.CommonView.RotationMode;
import kvv.kvvmap.view1.DiagramView;
import kvv.kvvmap.view1.KvvMapsButton;
import kvv.kvvmap.view1.MapView;
import kvv.kvvmap.view.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class MyActivity extends Activity {

	private static final String BUTTONS_VISIBLE_SETTING = "buttonsVisible";
	private static final String FOLLOW_GPS_SETTING = "followGPS";
	private static final String KINETIC_SCROLLING_SETTING = "kineticScrolling";
	private static final String LOAD_DURING_SCROLLING_SETTING = "loadDuringScrolling";
	private static final String LARGE_SETTING = "largeZoom";
	private static final String SPEED_PROFILE_SETTING = "speedProfile";
	private static final String DEBUG_DRAW_SETTING = "debugDraw";

	private static final String BRIGHTNESS_KEY = "brightness";
	private static final float DIMMED_BRIGHTNESS = 0.1f;

	public static final String PREFS_NAME = "KvvMapPrefsFile";

	private static final int MENU_LOGGING_ONOFF = 100;
	private static final int MENU_QUIT = 102;
	private static final int MENU_ADD_PLACEMARK = 105;
	private static final int MENU_KINETIC_SCROLLING = 106;
	private static final int MENU_LOAD_DURING_SCROLLING = 107;
	private static final int MENU_TRACKS = 108;
	private static final int MENU_LARGE = 109;

	private static final int MENU_DEBUG_DRAW = 110;
	private static final int MENU_ABOUT = 111;
	private static final int MENU_TOGGLE_BUTTONS = 112;
	private static final int MENU_SPEED_PROFILE = 113;

	private static final int MENU_UPDATE = 114;

	private MapView view;
	private DiagramView diagramView;
	private TextView altSpeed;

	private Adapter adapter;
	private PowerManager.WakeLock wakeLock;
	private SensorListener sensorListener;
	private SharedPreferences settings;
	public IKvvMapsService mapsService;

	private LocationManager locationManager;
	private LocationListener locationListener;

	private final Handler handler = new Handler();

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		int cnt = event.getRepeatCount();
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && cnt == 0) {
			view.reorderMaps();
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Adapter.log("MyActivity.onKeyDown");
		/*
		 * if (keyCode == KeyEvent.KEYCODE_BACK) { moveTaskToBack(true); return
		 * true; } else
		 */if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (view != null)
				view.decInfoLevel();
			updateButtons();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (view != null)
				view.incInfoLevel();
			updateButtons();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			zoomIn();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			zoomOut();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (event.getRepeatCount() == 0) {
			} else {
				editSel();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void onFG() {
		if (sensorListener == null) {
			sensorListener = new SensorListener() {
				public void onSensorChanged(int sensor, float[] values) {
					if (view != null)
						view.setCompass(values);
				}

				public void onAccuracyChanged(int sensor, int accuracy) {
				}
			};

		}

		((SensorManager) getSystemService(Context.SENSOR_SERVICE))
				.registerListener(sensorListener,
						SensorManager.SENSOR_ORIENTATION,
						SensorManager.SENSOR_DELAY_NORMAL);

		if (wakeLock == null) {
			wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
					.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
							"kvvMaps wake lock");
			wakeLock.acquire();
		}

		// handler.removeCallbacks(stopGPS);
		if (following())
			startGPS(false);
		updateButtons();

	}

	private void onBG() {
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}

		((SensorManager) getSystemService(Context.SENSOR_SERVICE))
				.unregisterListener(sensorListener);

		stopGPS();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		Adapter.log("onWindowFocusChanged " + hasFocus);

		if (hasFocus)
			onFG();
		else
			onBG();

		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onPause() {
		Adapter.log("onPause");
		onBG();

		super.onPause();
		System.gc();
	}

	@Override
	protected void onResume() {
		Adapter.log("onResume");

		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Adapter.log("onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (view != null) {
				Adapter.log("onServiceConnected " + view + " "
						+ MyActivity.this);
				mapsService = (IKvvMapsService) service;

				Environment envir = new Environment(adapter,
						mapsService.getPaths(), mapsService.getPlacemarks(),
						new Maps(adapter, mapsService.getMapsDir()));

				Bundle b = mapsService.getBundle();
				view.init(MyActivity.this, envir, b, getRotationMode());
				diagramView.init(envir.adapter);

				mapsService.setListener(new KvvMapsServiceListener() {
					@Override
					public void mapsLoaded() {
						if (view != null)
							view.repaint();
					}
				});

				// WindowManager.LayoutParams layoutParams2 = getWindow()
				// .getAttributes();
				// layoutParams2.screenBrightness = 0.5f;
				// getWindow().setAttributes(layoutParams2);

				if (b.getFloat(BRIGHTNESS_KEY) != 0.0f) {
					WindowManager.LayoutParams layoutParams = MyActivity.this
							.getWindow().getAttributes();
					layoutParams.screenBrightness = DIMMED_BRIGHTNESS;
					MyActivity.this.getWindow().setAttributes(layoutParams);
				}

				updateButtons();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			disconnectFromService();
		}
	};

	private void disconnectFromService() {
		if (mapsService != null) {
			mapsService.disconnect();
			mapsService = null;
		}
	}

	private void setLarge(boolean large) {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		adapter.setTileSize(large ? Adapter.TILE_SIZE_0 * 2
				: Adapter.TILE_SIZE_0, metrics.widthPixels,
				metrics.heightPixels);
		if (view != null)
			view.invalidateTiles();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Adapter.log("onCreate " + this);
		super.onCreate(savedInstanceState);

		setDefaultUncaughtExceptionHandler();

		settings = getSharedPreferences(PREFS_NAME, 0);

		if (!MapLoader.checkMaps(this))
			return;

		initTileSize();

		adapter = new Adapter(this);
		setLarge(settings.getBoolean(LARGE_SETTING, false));
		Adapter.debugDraw = settings.getBoolean("debugDraw", false);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		startService(new Intent(this, KvvMapsService.class));
		bindService(new Intent(this, KvvMapsService.class), conn,
				Context.BIND_AUTO_CREATE);

		setContentView(R.layout.screen);
		view = (MapView) findViewById(R.id.MapView);
		diagramView = (DiagramView) findViewById(R.id.diagramView);
		altSpeed = (TextView) findViewById(R.id.altSpeed);
		altSpeed.setBackgroundColor(0x80000000);
		altSpeed.setTextColor(COLOR.CYAN);
		altSpeed.setText("");

		setButtons();
	}

	private void setDefaultUncaughtExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			private UncaughtExceptionHandler defaultUEH = Thread
					.getDefaultUncaughtExceptionHandler();

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				PrintStream errPrintStream;
				try {
					OutputStream errOS = new FileOutputStream(Adapter.ROOT
							+ "/" + System.currentTimeMillis() + ".log");
					errPrintStream = new PrintStream(errOS, true);
					ex.printStackTrace(errPrintStream);
					errOS.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				defaultUEH.uncaughtException(thread, ex);
			}
		});
	}

	private void initTileSize() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int tileSz = 256;
		if (metrics.xdpi > 160)
			tileSz = (int) (tileSz * metrics.xdpi / 145);

		// tileSz = 384;
		Adapter.TILE_SIZE_0 = tileSz;
	}

	private void setButtons() {
		((KvvMapsButton) findViewById(R.id.edit)).setup(R.drawable.edit,
				R.drawable.edit_dis).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editSel();
			}
		});

		((KvvMapsButton) findViewById(R.id.info)).setup(R.drawable.info)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (view != null)
							view.toggleInfoLevel();
						updateButtons();
					}
				});
		;

		((KvvMapsButton) findViewById(R.id.zoomin)).setup(R.drawable.zoomin,
				R.drawable.zoomin_dis).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						zoomIn();
					}
				});
		;

		((KvvMapsButton) findViewById(R.id.zoomout)).setup(R.drawable.zoomout,
				R.drawable.zoomout_dis).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						zoomOut();
					}
				});
		;

		((KvvMapsButton) findViewById(R.id.rotate)).setup(R.drawable.rotate,
				R.drawable.rotate_dis).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (view != null)
							view.reorderMaps();
						updateButtons();
					}
				});
		;

		((KvvMapsButton) findViewById(R.id.gps)).setup(R.drawable.gps)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!following()) {
							gpsOn();
						} else {
							gpsOff();
							if (getRotationMode() == RotationMode.ROTATION_GPS)
								setRotationMode(RotationMode.ROTATION_NONE);
						}
					}
				});
		;

		((KvvMapsButton) findViewById(R.id.orient_gps)).setup(
				R.drawable.orient_gps).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (getRotationMode() == RotationMode.ROTATION_GPS)
							setRotationMode(RotationMode.ROTATION_NONE);
						else {
							gpsOn();
							setRotationMode(RotationMode.ROTATION_GPS);
						}
					}
				});
		;

		((KvvMapsButton) findViewById(R.id.orient_compass)).setup(
				R.drawable.orient_compass).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (getRotationMode() == RotationMode.ROTATION_COMPASS)
							setRotationMode(RotationMode.ROTATION_NONE);
						else
							setRotationMode(RotationMode.ROTATION_COMPASS);
					}
				});
		;

		((KvvMapsButton) findViewById(R.id.fixedmap))
				.setup(R.drawable.fixedmap).setOnClickListener(
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								fixUnfixMap();
							}
						});
		;

		((KvvMapsButton) findViewById(R.id.dimm)).setup(R.drawable.dimm)
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mapsService != null) {
							WindowManager.LayoutParams layoutParams = getWindow()
									.getAttributes();
							float br = mapsService.getBundle().getFloat(
									BRIGHTNESS_KEY);
							if (br != 0.0f) {
								mapsService.getBundle().putFloat(
										BRIGHTNESS_KEY, 0.0f);
								layoutParams.screenBrightness = br;
							} else {
								mapsService.getBundle().putFloat(
										BRIGHTNESS_KEY,
										layoutParams.screenBrightness);
								layoutParams.screenBrightness = DIMMED_BRIGHTNESS;
							}
							getWindow().setAttributes(layoutParams);
							updateButtons();
						}
					}
				});
		;

		ImageButton hereButton = (ImageButton) findViewById(R.id.here);
		hereButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (view != null)
					view.animateToMyLocation();
			}
		});

		Button toTarget = (Button) findViewById(R.id.toTarget);
		toTarget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (view != null)
					view.animateToTarget();
			}
		});

		toTarget.setBackgroundColor((COLOR.TARG_COLOR & 0x00FFFFFF) | 0x64000000);
		toTarget.setFocusable(false);

		updateButtons();
	}

	private boolean buttonsVisible() {
		return settings.getBoolean(BUTTONS_VISIBLE_SETTING, true);
	}

	private boolean following() {
		return settings.getBoolean(FOLLOW_GPS_SETTING, true);
	}

	private final Runnable buttonsUpdater = new Runnable() {
		@Override
		public void run() {
			if (view == null)
				return;

			View buttons = findViewById(R.id.screenButtons);
			if (buttonsVisible())
				buttons.setVisibility(View.VISIBLE);
			else
				buttons.setVisibility(View.GONE);

			((KvvMapsButton) findViewById(R.id.gps)).setCheched(following());

			RotationMode rot = getRotationMode();

			((KvvMapsButton) findViewById(R.id.orient_compass))
					.setCheched(rot == RotationMode.ROTATION_COMPASS);
			((KvvMapsButton) findViewById(R.id.orient_gps))
					.setCheched(rot == RotationMode.ROTATION_GPS);

			((KvvMapsButton) findViewById(R.id.info)).setCheched(view != null
					&& view.getInfoLevel() != InfoLevel.LOW);

			if (mapsService != null)
				findViewById(R.id.writing).setVisibility(
						mapsService.getTracker().isTracking() ? View.VISIBLE
								: View.GONE);

			((KvvMapsButton) findViewById(R.id.fixedmap))
					.setCheched(mapsService != null
							&& mapsService.getBundle().getString("fixedMap") != null);

			((KvvMapsButton) findViewById(R.id.zoomin)).setEnabled(view != null
					&& view.getZoom() < Utils.MAX_ZOOM);
			((KvvMapsButton) findViewById(R.id.zoomout))
					.setEnabled(view != null && view.getZoom() > Utils.MIN_ZOOM);

			View button = findViewById(R.id.here);
			if (view != null && view.getMyLocation() != null
					&& !view.isOnMyLocation())
				button.setVisibility(View.VISIBLE);
			else
				button.setVisibility(View.GONE);

			Button toTarget = (Button) findViewById(R.id.toTarget);
			LocationX target = view == null ? null : view.getTarget();
			if (target != null) {
				toTarget.setVisibility(View.VISIBLE);
				int dist = (int) view.getLocation().distanceTo(target);
				toTarget.setText(Utils.formatDistance(dist));
			} else {
				toTarget.setVisibility(View.GONE);
			}

			((KvvMapsButton) findViewById(R.id.rotate)).setEnabled(view != null
					&& view.isMultiple());

			((KvvMapsButton) findViewById(R.id.edit))
					.setVisibility(view != null
							&& view.getInfoLevel() != InfoLevel.LOW
							&& view.getSel() != null ? View.VISIBLE
							: View.INVISIBLE);

			if (view != null && view.getLocation() != null) {
				String lon = Utils.formatLatLon(view.getLocation()
						.getLongitude());
				String lat = Utils.formatLatLon(view.getLocation()
						.getLatitude());

				String title = lon + " " + lat + " z" + view.getZoom() + " "
						+ view.getTopMap();

				TextView text = (TextView) findViewById(R.id.mapInfo);
				text.setText(title);
			}

			((KvvMapsButton) findViewById(R.id.dimm))
					.setCheched(mapsService != null
							&& mapsService.getBundle().getFloat(BRIGHTNESS_KEY) != 0.0f);
		}
	};

	public void updateButtons() {
		handler.removeCallbacks(buttonsUpdater);
		handler.postDelayed(buttonsUpdater, 100);
	}

	private void zoomIn() {
		if (view != null)
			view.zoomIn();
		updateButtons();
	}

	private void zoomOut() {
		if (view != null)
			view.zoomOut();
		updateButtons();
	}

	private RotationMode getRotationMode() {
		return mapsService == null ? RotationMode.ROTATION_NONE : RotationMode
				.values()[mapsService.getBundle().getInt("rotation", 0)];
	}

	static class Option {
		int id;
		String text;
		String prefsName;
		ContextWrapper cw;
		boolean defaultValue;

		public Option(ContextWrapper cw, int id, String text, String prefsName,
				boolean defaultValue) {
			this.id = id;
			this.text = text;
			this.prefsName = prefsName;
			this.cw = cw;
			this.defaultValue = defaultValue;
		}

		public boolean get() {
			SharedPreferences settings = cw.getSharedPreferences(PREFS_NAME, 0);
			return settings == null ? defaultValue : settings.getBoolean(
					prefsName, defaultValue);
		}

		public void set(boolean value) {
			SharedPreferences settings = cw.getSharedPreferences(PREFS_NAME, 0);
			Editor prefsPrivateEditor = settings.edit();
			prefsPrivateEditor.putBoolean(prefsName, value);
			prefsPrivateEditor.commit();
		}

		void toggle() {
			set(!get());
		}
	}

	Option[] options = new Option[] {
			new Option(this, MENU_DEBUG_DRAW, "debug info", DEBUG_DRAW_SETTING,
					false) {
				@Override
				public void set(boolean value) {
					super.set(value);
					Adapter.debugDraw = value;
					if (view != null)
						view.invalidateTiles();
				}
			},
			new Option(this, MENU_TOGGLE_BUTTONS, "Экранные кнопки",
					BUTTONS_VISIBLE_SETTING, true),
			new Option(this, MENU_KINETIC_SCROLLING, "Плавная прокрутка",
					KINETIC_SCROLLING_SETTING, true),
			new Option(this, MENU_LOAD_DURING_SCROLLING,
					"Подгружать при прокрутке", LOAD_DURING_SCROLLING_SETTING,
					true),
			new Option(this, MENU_LARGE, "Крупный размер", LARGE_SETTING, false) {
				public void set(boolean value) {
					super.set(value);
					setLarge(!get());
				};
			},
			new Option(this, MENU_SPEED_PROFILE, "Скоростной профиль",
					SPEED_PROFILE_SETTING, false), };

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.findItem(MENU_LOGGING_ONOFF).setChecked(
				mapsService != null && mapsService.getTracker().isTracking());
		if (mapsService != null && mapsService.getTracker().isTracking())
			menu.findItem(MENU_LOGGING_ONOFF).setTitle("Запись пути выкл");
		else
			menu.findItem(MENU_LOGGING_ONOFF).setTitle("Запись пути");

		for (Option option : options)
			menu.findItem(option.id).setChecked(option.get());

		return super.onPrepareOptionsMenu(menu);
	}

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.addSubMenu(0, MENU_PATHS, order, title)

		menu.add(Menu.NONE, MENU_LOGGING_ONOFF, 0, "Запись пути").setCheckable(
				true);
		menu.add(Menu.NONE, MENU_ADD_PLACEMARK, 0, "Добавить точку");
		menu.add(Menu.NONE, MENU_TRACKS, 0, "Пути");

		SubMenu settingsSubMenu = menu.addSubMenu("Настройки");

		for (Option option : options)
			settingsSubMenu.add(Menu.NONE, option.id, 0, option.text)
					.setCheckable(true);

		menu.add(Menu.NONE, MENU_ABOUT, 0, "О программе");
		menu.add(Menu.NONE, MENU_UPDATE, 0, "Update");
		menu.add(Menu.NONE, MENU_QUIT, 0, "Выход");

		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {

		for (Option option : options)
			if (option.id == item.getItemId()) {
				option.toggle();
				return true;
			}

		switch (item.getItemId()) {
		case MENU_LOGGING_ONOFF:
			trackingOnOff();
			return true;
		case MENU_ADD_PLACEMARK:
			addPlacemark();
			return true;
		case MENU_TRACKS:
			paths();
			return true;
		case MENU_ABOUT:
			about();
			return true;
		case MENU_UPDATE:
			try {
				updateSoftware();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		case MENU_QUIT:
			stopGPS();
			stopService(new Intent(this, KvvMapsService.class));
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					System.exit(0);
				}
			}, 1000);
			finish();
			return true;
		default:
		}
		return false;
	}

	private void setRotationMode(RotationMode rotationMode) {
		if (mapsService == null)
			return;
		mapsService.getBundle().putInt("rotation", rotationMode.ordinal());
		if (view != null)
			view.setRotationMode(rotationMode);
		updateButtons();
	}

	private void updateSoftware() throws MalformedURLException, IOException {
		startActivity(new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://palermo.ru/vladimir/kvvMaps/kvvMaps.apk")));
	}

	// private void updateSoftware1() throws MalformedURLException, IOException
	// {
	// startActivity(new Intent(Intent.ACTION_VIEW,
	// Uri.parse("http://palermo.ru/vladimir/kvvMaps.apk")));
	// }

	private void fixUnfixMap() {
		if (mapsService == null || view == null)
			return;
		String fixedMap = mapsService.getBundle().getString("fixedMap");
		if (fixedMap == null)
			fixedMap = view.getTopMap();
		else
			fixedMap = null;

		view.fixMap(fixedMap);
		mapsService.getBundle().putString("fixedMap", fixedMap);

		// fixedMap = view.fixMap(fixedMap == null);
		// mapsService.getBundle().putString("fixedMap", fixedMap);
		updateButtons();
	}

	public String getFixedMap() {
		if (mapsService == null)
			return null;
		return mapsService.getBundle().getString("fixedMap");
	}

	private void about() {

		try {
			String app_ver = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0).versionName;
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("About");
			alertDialog.setMessage("KvvMaps\nversion: " + app_ver
					+ "\nVladimir Krupsky\nv_krupsky@mail.ru");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			alertDialog.show();
		} catch (NameNotFoundException e) {
		}
	}

	static class PathLocPair extends Pair<Path, LocationX> {
		public PathLocPair(Path first, LocationX second) {
			super(first, second);
		}
	}

	private void paths() {
		final Path[] paths = mapsService.getPaths().getPaths()
				.toArray(new Path[0]);

		final PathLocPair[] paths1 = new PathLocPair[paths.length];

		for (int i = 0; i < paths.length; i++)
			paths1[i] = new PathLocPair(paths[i], paths[i].getNearest(view
					.getCenter()));

		Arrays.sort(paths1, new Comparator<PathLocPair>() {
			public int compare(PathLocPair path1, PathLocPair path2) {
				LocationX pm1 = path1.second;
				LocationX pm2 = path2.second;
				if (pm1 == null)
					return 1;
				if (pm2 == null)
					return -1;

				double d1 = pm1.distanceTo(view.getCenter());
				double d2 = pm2.distanceTo(view.getCenter());
				if (d1 < d2)
					return -1;
				if (d1 > d2)
					return 1;
				return 0;
			}
		});

		final String[] names = new String[paths1.length];
		for (int i = 0; i < paths1.length; i++)
			names[i] = paths1[i].first.getName();

		final boolean[] checks = new boolean[paths1.length];
		for (int i = 0; i < paths1.length; i++)
			checks[i] = paths1[i].first.isEnabled();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Paths");
		builder.setMultiChoiceItems(names, checks,
				new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						checks[which] = isChecked;
						paths1[which].first.setEnabled(isChecked);
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}

	public boolean isGPS() {
		return locationListener != null;
	}

	private void startGPS(final boolean fromMenu) {
		if (locationListener == null) {
			locationListener = new LocationListener() {
				public void onStatusChanged(String provider, int status,
						Bundle extras) {
					if (status == LocationProvider.OUT_OF_SERVICE) {
						Adapter.log("GPS onStatusChanged " + status);
						// stopFollow();
					}
				}

				public void onProviderEnabled(String provider) {
					Adapter.log("GPS onProviderEnabled " + provider);
				}

				public void onProviderDisabled(String provider) {
					Adapter.log("GPS onProviderDisabled " + provider);
					// stopFollow();
				}

				private boolean scroll = fromMenu;

				public void onLocationChanged(Location location) {
					Adapter.log("GPS onLocationChanged ");
					LocationX loc = new LocationX(location);
					if (view != null)
						view.setMyLocation(loc, scroll);
					updateButtons();
					scroll = false;

					altSpeed.setText("" + (int) loc.getAltitude() + "m "
							+ (int) (loc.getSpeed() * 3.6f) + "km/h");
				}
			};

			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 5000L, 20.0f,
					locationListener);

			Adapter.log("GPS requestLocationUpdates " + locationListener);
		}
	}

	private void stopGPS() {
		if (locationListener != null) {
			altSpeed.setText("");
			locationManager.removeUpdates(locationListener);
			Adapter.log("GPS removeUpdates " + locationListener);
			locationListener = null;
			if (view != null)
				view.dimmMyLocation();
			updateButtons();
		}
	}

	// private Runnable stopGPS = new Runnable() {
	// @Override
	// public void run() {
	// stopGPS();
	// }
	// };

	private void gpsOn() {
		startGPS(true);
		Editor prefsPrivateEditor = settings.edit();
		prefsPrivateEditor.putBoolean(FOLLOW_GPS_SETTING, true);
		prefsPrivateEditor.commit();
		updateButtons();
		updateView();
	}

	private void gpsOff() {
		stopGPS();
		Editor prefsPrivateEditor = settings.edit();
		prefsPrivateEditor.putBoolean(FOLLOW_GPS_SETTING, false);
		prefsPrivateEditor.commit();
		updateButtons();
		updateView();
	}

	private void addPlacemark() {
		if (view == null)
			return;
		final LocationX pm = view.createPlacemark();
		if (pm != null) {
			PlaceMarkDlg dlg = new PlaceMarkDlg(this, view, pm,
					mapsService.getPlacemarks(), PlaceMarkDlg.Type.ADD);
			dlg.show();
		}
	}

	public void editSel() {
		if (view == null)
			return;

		ISelectable sel = view.getSel();
		if (sel == null)
			return;

		if (sel instanceof LocationX) {
			LocationX pm = (LocationX) sel;
			PlaceMarkDlg dlg = new PlaceMarkDlg(this, view, pm,
					mapsService.getPlacemarks(), PlaceMarkDlg.Type.EDIT);
			dlg.show();
		}

		if (sel instanceof PathSelection) {
			PathSelection pathSel = (PathSelection) sel;
			new PathDlg(this, pathSel.path, pathSel.pm, mapsService.getPaths())
					.show();
		}
		updateButtons();
	}

	private void trackingOnOff() {
		if (mapsService == null)
			return;
		if (mapsService.getTracker().isTracking()) {
			mapsService.getTracker().endPath();
		} else {
			mapsService.getTracker().startPath();
		}
		updateButtons();
		updateView();
	}

	public boolean isKineticScrolling() {
		return settings == null
				|| settings.getBoolean(KINETIC_SCROLLING_SETTING, true);
	}

	public boolean loadDuringScrolling() {
		return settings == null
				|| settings.getBoolean(LOAD_DURING_SCROLLING_SETTING, true);
	}

	@Override
	protected void onDestroy() {
		if (view != null && mapsService != null) {
			Bundle b = mapsService.getBundle();
			view.save(b);
		}
		// locationManager = null;

		Adapter.log("onDestroy");

		if (mapsService != null) {
			unbindService(conn);
			disconnectFromService();
		}

		if (view != null)
			view.dispose();
		if (diagramView != null)
			diagramView.dispose();

		view = null;
		diagramView = null;

		if (adapter != null) {
			adapter.recycle();
			adapter = null;
		}

		super.onDestroy();
		System.runFinalizersOnExit(true);

		Drawables.unbindDrawables(findViewById(R.id.RootView));

		System.gc();

	}

	public void updateView() {
		if (view != null)
			view.invalidate();
	}

	public void pathSelected(PathSelection sel) {
		if (diagramView != null) {
			diagramView.speedProfile = settings.getBoolean(
					SPEED_PROFILE_SETTING, false);
			diagramView.pathSelected(view != null
					&& view.getInfoLevel() == InfoLevel.HIGH ? sel : null);
		}
	}

}