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

		KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		if (event == null)
			return;

		System.out.println("KeyCode " + event.getKeyCode());
		abortBroadcast();

		if (APService.staticInstance == null)
			return;

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				if (APService.staticInstance.isPlaying())
					APService.staticInstance.pause();
				else
					APService.staticInstance.play();
				break;
			case KeyEvent.KEYCODE_MEDIA_PLAY:
				APService.staticInstance.play();
				break;
			case KeyEvent.KEYCODE_HEADSETHOOK:
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				APService.staticInstance.pause();
				break;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				APService.staticInstance.next();
				break;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				APService.staticInstance.prev();
				break;

//			case KeyEvent.KEYCODE_MEDIA_REWIND:
//				APService.staticInstance.seekTo(Math.max(0, APService.staticInstance.getCurrentPosition() - 10000));
//				break;
//
//			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
//				APService.staticInstance.prev();
//				break;
			}
		}
		if (event.getAction() == KeyEvent.ACTION_MULTIPLE) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				APService.staticInstance.next();
				break;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				APService.staticInstance.seekTo(Math.max(0, APService.staticInstance.getCurrentPosition() - 1000));
//				APService.staticInstance.prev();
				break;

//			case KeyEvent.KEYCODE_MEDIA_REWIND:
//				APService.staticInstance.seekTo(Math.max(0, APService.staticInstance.getCurrentPosition() - 10000));
//				break;
//
//			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
//				APService.staticInstance.prev();
//				break;
			}
		}

	}

}
