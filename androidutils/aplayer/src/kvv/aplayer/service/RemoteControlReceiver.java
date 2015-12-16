package kvv.aplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {

		String intentAction = intent.getAction();
		System.out.println("INTENT_ACTION " + intent.getAction());

		if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction))
			return;

		KeyEvent event = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		if (event == null)
			return;

		System.out.println("KeyCode " + event.getKeyCode());

		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_HEADSETHOOK:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
		case KeyEvent.KEYCODE_MEDIA_PLAY:
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (APService.staticInstance != null)
					APService.staticInstance.play_pause();
			}
			break;
		}

		abortBroadcast();
	}

}
