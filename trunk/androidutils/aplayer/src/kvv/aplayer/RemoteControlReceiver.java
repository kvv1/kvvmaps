package kvv.aplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver {

	private static long pressedTime;

	@Override
	public void onReceive(Context context, Intent intent) {

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

		if (event.getAction() == 0) {
			if (pressedTime == 0) {
				pressedTime = System.currentTimeMillis();
				context.sendBroadcast(new Intent()
						.setAction("kvv.aplayer.PLAY_PAUSE"));
				ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
				toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200); 
			} else if (pressedTime > 0) {
				if (System.currentTimeMillis() > pressedTime + 1000) {
					pressedTime = -1;
					context.sendBroadcast(new Intent()
							.setAction("kvv.aplayer.NEXT"));
					ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
					toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200); 
				}
			}
		}

		if (event.getAction() == 1)
			pressedTime = 0;
	}

}
