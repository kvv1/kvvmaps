package kvv.aplayer.files;

import kvv.aplayer.R;
import kvv.aplayer.files.tape.LevelView;
import kvv.aplayer.files.tape.TapeView;
import kvv.aplayer.player.Files;
import kvv.aplayer.player.Player.PlayerAdapter;
import kvv.aplayer.player.Player.PlayerListener;
import kvv.aplayer.player.Player1.PlayerLevelListener;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.IAPService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;

import com.smartbean.androidutils.util.Utils;

public class FilesSectionFragmentTape extends FilesSectionFragmentBase {

	private TapeView tapeView;
	private LevelView arrowLevelView;
	private LevelView magicEyeLevelView;

	private PlayerLevelListener levelListener = new PlayerLevelListener() {
		@Override
		public void levelChanged(float indicatorLevel) {
			if (arrowLevelView != null)
				arrowLevelView.setLevel(indicatorLevel);
			if (magicEyeLevelView != null)
				magicEyeLevelView.setLevel(indicatorLevel);
		}
	};

	private PlayerListener listener = new PlayerAdapter() {
		@Override
		public void fileChanged() {
			startStopAnim();
		}
	};

	public FilesSectionFragmentTape() {
		super(APService.class, R.layout.fragment_files_tape);
		Utils.log(this, "CTR");
	}

	protected void createUI(final IAPService service) {
		Utils.log(this, "createUI");
		super.createUI(service);

		tapeView = (TapeView) rootView.findViewById(R.id.tape);

		arrowLevelView = (LevelView) rootView.findViewById(R.id.arrowLevel);
		arrowLevelView.setScale(new int[] { -20, -10, -6, -3, 0, 3 });
		magicEyeLevelView = (LevelView) rootView.findViewById(R.id.magicEyeLevel);
		magicEyeLevelView.setScale(new int[] { -20, -10, -6, -3, 0, 3 });

		new TouchListener(tapeView) {
			@Override
			protected void onClick(float touchX, float touchY) {
				if (conn.service == null)
					return;
				int ht = tapeView.hitTest(touchX, touchY);
				if (ht == -1) {
					tapeView.setSeek(-2000, true);
					rootView.getContext().sendBroadcast(new Intent().setAction(Intent.ACTION_MEDIA_BUTTON).putExtra(
							Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS)));
				} else if (ht == 1) {
					Files files = conn.service.getFiles();
					if (files.curFile < files.files.size() - 1)
						tapeView.setSeek(2000, true);
					rootView.getContext().sendBroadcast(new Intent().setAction(Intent.ACTION_MEDIA_BUTTON).putExtra(
							Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)));
				} else {
					rootView.getContext().sendBroadcast(new Intent().setAction(Intent.ACTION_MEDIA_BUTTON).putExtra(
							Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)));
				}
			}

			@Override
			protected void onLongClick(float touchX, float touchY) {
				int ht = tapeView.hitTest(touchX, touchY);
				if (ht == 0)
					showUndoRedoPanel();
			}

			@Override
			protected void onReleased(float touchX, float touchY) {
				if (tapeView != null)
					tapeView.setSeek(0, false);
			}
		};

		rootView.findViewById(R.id.bottomButtons).setVisibility(View.GONE);

		conn.service.addListener(listener);
		listener.folderChanged();
		listener.fileChanged();

		startStopAnim();
	}

	protected void setFolderProgress(int max, int cur) {
		super.setFolderProgress(max, cur);
		if (tapeView != null)
			tapeView.setProgress(max, cur);
	}

	@Override
	public void onDestroy() {
		if (conn.service != null) {
			conn.service.removeLevelListener(levelListener);
			conn.service.removeListener(listener);
		}
		super.onDestroy();
	}

	@Override
	public void onPause() {
		System.out.println("ONPAUSE");
		super.onPause();
	}

	@Override
	public void onResume() {
		System.out.println("ONRESUME");
		super.onResume();
	}

	@Override
	protected void startStopAnim() {
		super.startStopAnim();

		if (tapeView == null)
			return;

		boolean magicEye = settings.getBoolean(getString(R.string.prefMagicEye), false);

		arrowLevelView.setVisibility(magicEye ? View.GONE : View.VISIBLE);
		magicEyeLevelView.setVisibility(magicEye ? View.VISIBLE : View.GONE);

		if (tapeView != null)
			tapeView.click = settings.getBoolean(getString(R.string.prefClick), false);

		if (resumed && visible && conn.service != null && conn.service.isPlaying()) {
			System.out.println("tapeView.start");
			conn.service.addLevelListener(levelListener);
			tapeView.start();
		} else {
			if (conn.service != null)
				conn.service.removeLevelListener(levelListener);
			System.out.println("tapeView.stop");
			tapeView.stop();
		}
	}

}
