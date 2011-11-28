package kvv.mailru;

import kvv.mailru.CounterService.CounterServiceBinder;
import kvv.mailru.CounterService.CounterServiceHandler;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MailRuActivity extends Activity {

	private CounterServiceHandler serviceHandler = new CounterServiceHandler() {
		public void onCounterChanged(int n) {
			((TextView) findViewById(R.id.status)).setText(Integer.toString(n));
		}
	};

	private CounterServiceBinder service;
	private final ServiceConnection connection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder boundService) {
			register((CounterServiceBinder) boundService);
		}

		public void onServiceDisconnected(ComponentName name) {
			unregister();
		}
	};

	private void register(CounterServiceBinder service) {
		this.service = service;
		service.addHandler(serviceHandler);
	}

	private void unregister() {
		if (service != null)
			service.removeHandler(serviceHandler);
		service = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		findViewById(R.id.start).setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				startService(new Intent(MailRuActivity.this, CounterService.class));
			}
		});

		findViewById(R.id.stop).setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				unbindService(connection);
				unregister();
				stopService(new Intent(MailRuActivity.this, CounterService.class));
				bindService(new Intent(MailRuActivity.this, CounterService.class),
						connection, Service.BIND_AUTO_CREATE);
			}
		});

		findViewById(R.id.set).setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				EditText number = (EditText) findViewById(R.id.number);
				try {
					int n = Integer.parseInt(number.getText().toString());
					Intent i = new Intent();
					i.setAction(CounterService.SET_COUNTER_INTENT);
					i.putExtra(CounterService.SET_COUNTER_INTENT_PARAM, n);
					sendBroadcast(i);

				} catch (NumberFormatException e) {
					Toast.makeText(MailRuActivity.this,
							"illegal number format", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		bindService(new Intent(MailRuActivity.this, CounterService.class),
				connection, Service.BIND_AUTO_CREATE);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unbindService(connection);
		unregister();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void finalize() throws Throwable {
		Log.w("XXX", "~activity");
		super.finalize();
	}

}