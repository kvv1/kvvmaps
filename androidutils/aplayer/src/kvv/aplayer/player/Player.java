package kvv.aplayer.player;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;

public abstract class Player {

	public enum OnChangedHint {
		FILE, FOLDER, STATE
	}

	public abstract void onChanged(OnChangedHint hint);

	private final MediaPlayer mp = new MediaPlayer();

	private boolean prepared;

	public Player() {
		mp.setVolume(1, 1);

		mp.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				onChanged(OnChangedHint.FOLDER);
				prepared = false;
				return false;
			}
		});

		mp.setOnInfoListener(new OnInfoListener() {
			@Override
			public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
				onChanged(OnChangedHint.FOLDER);
				return false;
			}
		});

	}

	protected MediaPlayer getMP() {
		return mp;
	}

	protected void setOnCompletionListener(OnCompletionListener listener) {
		mp.setOnCompletionListener(listener);
	}

	protected void toFile(String path, int pos, boolean start) {
		try {
			if (mp.isPlaying())
				mp.stop();

			mp.reset();
			prepared = false;

			mp.setDataSource(path);
			mp.prepare();

			prepared = true;

			mp.seekTo(pos);
			if (start)
				mp.start();

			onChanged(OnChangedHint.FILE);

			resetGain();
		} catch (Exception e) {
			prepared = false;
		}
	}

	public void seekTo(int pos) {
		if (!prepared)
			return;
		mp.seekTo(pos);
		onChanged(OnChangedHint.STATE);
	}

	public void pause() {
		if (!prepared)
			return;
		if (mp.isPlaying()) {
			mp.pause();
			onChanged(OnChangedHint.STATE);
		}
	}

	public void play() {
		if (!prepared)
			return;
		if (!mp.isPlaying()) {
			resetGain();
			mp.start();
			onChanged(OnChangedHint.STATE);
		}
	}

	public int getDuration() {
		// System.out.println("getDuration()");
		if (!prepared)
			return 1;
		return mp.getDuration();
	}

	public int getCurrentPosition() {
		// System.out.println("getCurrentPosition()");
		if (!prepared)
			return 0;
		return mp.getCurrentPosition();
	}

	public boolean isPlaying() {
		// System.out.println("isPlaying()");
		if (!prepared)
			return false;
		return mp.isPlaying();
	}

	public void close() {
		prepared = false;
		mp.release();
	}

	protected void resetGain() {
	}
}
