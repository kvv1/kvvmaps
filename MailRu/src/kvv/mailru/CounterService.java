package kvv.mailru;

import java.util.Collection;
import java.util.HashSet;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class CounterService extends Service {

	public interface CounterServiceHandler {
		void onCounterChanged(int n);
	}
	
	public static final String SET_COUNTER_INTENT = "kvv.mailru.SET_COUNTER_INTENT";
	public static final String SET_COUNTER_INTENT_PARAM = "number";

	private static final long PERIOD = 1000;

	private int counter;
	private PowerManager.WakeLock wakeLock;
	private final CounterServiceBinder binder = new CounterServiceBinder();
	private final Handler timerHandler = new Handler();

	private final Collection<CounterServiceHandler> handlers = new HashSet<CounterServiceHandler>();

	private void setCounter(int n) {
		counter = n;
		for (CounterServiceHandler h : handlers)
			h.onCounterChanged(counter);
	}

	class CounterServiceBinder extends Binder {
		public void addHandler(CounterServiceHandler h) {
			handlers.add(h);
		}

		public void removeHandler(CounterServiceHandler h) {
			handlers.remove(h);
		}
	}

	private final Runnable timerRunnable = new Runnable() {
		public void run() {
			setCounter(counter + 1);
			timerHandler.postDelayed(this, PERIOD);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		timerHandler.removeCallbacks(timerRunnable);
		if (wakeLock != null)
			wakeLock.release();
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (wakeLock == null) {
			wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
					.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
							"MyService wake lock");
			wakeLock.acquire();
		}
		timerHandler.removeCallbacks(timerRunnable);
		timerHandler.postDelayed(timerRunnable, PERIOD);

		setCounter(intent.getIntExtra(SET_COUNTER_INTENT_PARAM, 0));
	}
	
	@Override
	protected void finalize() throws Throwable {
		Log.w("XXX", "~service");
		super.finalize();
	}
}
