package kvv.AndroidController;

import kvv.httpserver.HTTPHandler;
import kvv.httpserver.HTTPServer;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AndroidController extends Activity {
	private final ConterollerAPI api = new ConterollerAPI();

	private PowerManager.WakeLock wakeLock;

	private HTTPServer server = new HTTPServer();

	@Override
	protected void onPause() {
		wakeLock.release();
		super.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();
		wakeLock.acquire();
	}

	@Override
	protected void onDestroy() {
		server.close();
		server.interrupt();
		api.stop();
		super.onDestroy();
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		System.out.println("IP " + HTTPServer.getLocalIpAddress());

//		server.addHandler(new HTTPHandler("/hello") {
//			@Override
//			public String handle(String queryString) {
//				return "HELLO " + queryString;
//			}
//		});
//
//		server.addHandler(new HTTPHandler("/preved") {
//			@Override
//			public String handle(String queryString) {
//				return "PREVED " + queryString;
//			}
//		});
//
//		server.start();

		wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");

		setContentView(R.layout.main);
		api.start();

		Button on0 = (Button) findViewById(R.id.on0);
		on0.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				api.setOutput(0, true);
			}
		});

		Button off0 = (Button) findViewById(R.id.off0);
		off0.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				api.setOutput(0, false);
			}
		});

		Button on1 = (Button) findViewById(R.id.on1);
		on1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				api.setOutput(1, true);
			}
		});

		Button off1 = (Button) findViewById(R.id.off1);
		off1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				api.setOutput(1, false);
			}
		});

		Button temp = (Button) findViewById(R.id.temp);
		temp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Float t = api.getTemp(0);
				TextView tempStr = (TextView) findViewById(R.id.tempStr);
				if (t == null)
					tempStr.setText("ERROR");
				else
					tempStr.setText(t.toString());
			}
		});

	}
}