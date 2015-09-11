package kvv.aplayer.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kvv.aplayer.folders.Folder;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;

public abstract class Player {

	protected abstract void onChanged();

	protected abstract void onRandomChanged();

	private void onChanged1() {
		onChanged();
	}

	protected final MediaPlayer mp = new MediaPlayer();

	private boolean initialized;

	private List<Folder> folders;
	private int curFolder = -1;
	private int curFile = 0;

	public Player(List<Folder> folders) {

		this.folders = folders;
		folders.add(new Folder("RANDOM", 0, new String[0]));

		mp.setVolume(1, 1);

		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				System.out.println("onCompletion");
				if (Player.this.folders.size() == 0 || curFolder < 0)
					return;
				Folder folder = Player.this.folders.get(curFolder);
				if (curFile >= folder.files.length - 1) {
					mp.stop();
					curFile = 0;
					reload(false);
					// mp.reset();
					onChanged1();
					return;
				}

				next(true);
				onChanged1();
			}
		});

		mp.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				onChanged1();
				return false;
			}
		});

		mp.setOnInfoListener(new OnInfoListener() {
			@Override
			public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
				onChanged1();
				return false;
			}
		});

		onChanged1();
	}

	private void toFile(int idx, int pos, boolean forcePlay) {
		if (folders.size() == 0 || curFolder < 0)
			return;

		Folder folder = folders.get(curFolder);
		if (idx >= folder.files.length)
			curFile = 0;
		else
			curFile = idx;

		reload(forcePlay);
		mp.seekTo(pos);
	}

	private void reload(boolean forcePlay) {
		try {
			boolean playing = mp.isPlaying();
			if (playing)
				mp.stop();
			mp.reset();
			setDataSource();
			initialized = true;
			mp.prepare();
			resetGain();
			if (playing || forcePlay)
				mp.start();
		} catch (Exception e) {
		}
	}

	private void setDataSource() throws Exception {
		if (folders.size() == 0 || curFolder < 0)
			return;
		if (curFolder >= folders.size())
			return;
		Folder folder = folders.get(curFolder);
		if (curFile >= folder.files.length)
			return;
		mp.setDataSource(folder.files[curFile]);
	}

	public void setVolume(float v) {
		// mp.setVolume(v, v);
	}

	public void makeRandom(int folderIdx) {
		if (folders.size() == 0)
			return;

		Folder folder = folders.get(folderIdx);

		List<String> files = new ArrayList<String>();
		files.addAll(Arrays.asList(folder.files));
		for (int i = folderIdx + 1; i < folders.size(); i++) {
			Folder f = folders.get(i);
			if (f.indent <= folder.indent)
				break;
			files.addAll(Arrays.asList(f.files));
		}

		Collections.shuffle(files);

		Folder randFolder = folders.get(folders.size() - 1);

		randFolder.files = files.toArray(new String[0]);
		randFolder.displayName = folder.displayName + " RND";

		onRandomChanged();
		toFolder(folders.size() - 1, 0, 0);
	}

	public List<Folder> getFolders() {
		return folders;
	}

	public int getCurrentFolder() {
		return curFolder;
	}

	public void toFolder(int folder, int file, int curPos) {
		if (folders.size() == 0)
			return;

		curFolder = folder;
		toFile(file, curPos, true);
		onChanged1();
	}

	public void toFile(int idx) {
		toFile(idx, 0, true);
		onChanged1();
	}

	public void seek(int seekStep) {
		if (folders.size() == 0 || curFolder < 0)
			return;

		if (!mp.isPlaying())
			return;

		Folder folder = folders.get(curFolder);

		int cur = mp.getCurrentPosition();

		if (seekStep < 0) {
			seekStep = -seekStep;
			if (cur < seekStep && curFile > 0) {
				toFile(curFile - 1);
				mp.seekTo(Math.max(0, mp.getDuration() - seekStep));
			} else {
				mp.seekTo(Math.max(0, cur - seekStep));
			}
		} else {
			if (cur + seekStep >= mp.getDuration()) {
				if (curFile < folder.files.length - 1) {
					toFile(curFile + 1);
				}
			} else {
				mp.seekTo(cur + seekStep);
			}
		}
		onChanged1();
	}

	public void prev() {
		if (folders.size() == 0 || curFolder < 0)
			return;

		int cur = mp.getCurrentPosition();
		if (cur < 3000 && curFile > 0)
			toFile(curFile - 1, 0, true);
		else
			reload(false);
		onChanged1();
	}

	public void next(boolean forcePlay) {
		if (folders.size() == 0 || curFolder < 0)
			return;

		Folder folder = folders.get(curFolder);
		if (curFile >= folder.files.length - 1) {
			return;
		}
		toFile(curFile + 1, 0, forcePlay);
		onChanged1();
	}

	public void pause() {
		if (mp.isPlaying()) {
			mp.pause();
			onChanged1();
		}
	}

	public void play_pause() {
		resetGain();
		if (mp.isPlaying())
			mp.pause();
		else
			mp.start();
		onChanged1();
	}

	public int getDuration() {
		if (!initialized)
			return 0;
		return mp.getDuration();
	}

	public int getCurrentPosition() {
		if (!initialized)
			return 0;
		return mp.getCurrentPosition();
	}

	public int getFile() {
		return curFile;
	}

	public boolean isPlaying() {
		return mp.isPlaying();
	}

	public void close() {
		mp.release();
	}

	protected void resetGain() {
	}
}
