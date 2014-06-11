package kvv.aplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.Toast;

public class RemoteControlReceiver extends BroadcastReceiver {

	private static long pressedTime;

	public void onReceive1(Context context, Intent intent) {

		String intentAction = intent.getAction();
		System.out.println("INTENT_ACTION " + intent.getAction());

		if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			return;
		}
		KeyEvent event = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		if (event == null) {
			return;
		}

		if (event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK) {
			if (event.getAction() == 0) {
				if (pressedTime == 0) {
					pressedTime = System.currentTimeMillis();
					context.sendBroadcast(new Intent()
							.setAction("kvv.aplayer.PLAY_PAUSE"));
					ToneGenerator toneG = new ToneGenerator(
							AudioManager.STREAM_ALARM, 100);
					toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
							200);
				} else if (pressedTime > 0) {
					if (System.currentTimeMillis() > pressedTime + 1500) {
						pressedTime = -1;
						context.sendBroadcast(new Intent()
								.setAction("kvv.aplayer.NEXT"));
						ToneGenerator toneG = new ToneGenerator(
								AudioManager.STREAM_ALARM, 100);
						toneG.startTone(
								ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
					}
				}
			}

			if (event.getAction() == 1)
				pressedTime = 0;
		}

		// abortBroadcast();
	}

	static int d = 0;

	public void onReceive(Context context, Intent intent) {
		String intentAction = intent.getAction();
		if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			return;
		}
		KeyEvent event = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		if (event == null) {
			return;
		}
		int action = event.getAction();
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_HEADSETHOOK:
			if (action == KeyEvent.ACTION_DOWN) {
				d++;
				Handler handler = new Handler();
				Runnable r = new Runnable() {

					@Override
					public void run() {
						if (d == 1) {
							System.out.println("single click!");
						}
						// double click *********************************
						if (d == 2) {
							System.out.println("double click!");
						}
						d = 0;
					}
				};
				if (d == 1) {
					handler.postDelayed(r, 500);
				}
			}
			break;
		}
		//abortBroadcast();
	}
}
