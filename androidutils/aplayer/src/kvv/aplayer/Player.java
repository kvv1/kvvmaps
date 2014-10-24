package kvv.aplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;

public abstract class Player {

	protected abstract void onChanged();
	protected abstract void onRandomChanged();

	private MediaPlayer mp = new MediaPlayer();
	private boolean initialized;

	private List<Folder> folders;
	private int curFolder = -1;
	private int curFile = 0;

	public Player(List<Folder> folders) {
		this.folders = folders;
		folders.add(new Folder("RANDOM", 0, new File[0]));

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
					restart(false);
					//mp.reset();
					onChanged();
					return;
				}
				
				next(true);
				onChanged();
			}
		});

		mp.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				onChanged();
				return false;
			}
		});

		mp.setOnInfoListener(new OnInfoListener() {
			@Override
			public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
				onChanged();
				return false;
			}
		});

		// mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {
		// @Override
		// public void onSeekComplete(MediaPlayer mp) {
		// onChanged();
		// }
		// });
	}

	public void setMaxVolume() {
		mp.setVolume(1, 1);
	}

	private void setDataSource() throws Exception {
		if (folders.size() == 0 || curFolder < 0)
			return;
		if (curFolder >= folders.size())
			return;
		Folder folder = folders.get(curFolder);
		if (curFile >= folder.files.length)
			return;
		mp.setDataSource(folder.files[curFile].getAbsolutePath());
	}

	public void makeRandom(int folderIdx) {
		if (folders.size() == 0)
			return;

		Folder folder = folders.get(folderIdx);

		List<File> files = new ArrayList<File>();
		files.addAll(Arrays.asList(folder.files));
		for (int i = folderIdx + 1; i < folders.size(); i++) {
			Folder f = folders.get(i);
			if (f.indent <= folder.indent)
				break;
			files.addAll(Arrays.asList(f.files));
		}

		Collections.shuffle(files);

		Folder randFolder = folders.get(folders.size() - 1);

		randFolder.files = files.toArray(new File[0]);

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
		onChanged();
	}

	public void toFile(int idx) {
		toFile(idx, 0, true);
		onChanged();
	}

	private void toFile(int idx, int pos, boolean forcePlay) {
		if (folders.size() == 0 || curFolder < 0)
			return;

		Folder folder = folders.get(curFolder);
		if (idx >= folder.files.length)
			curFile = 0;
		else
			curFile = idx;

		restart(forcePlay);

		mp.seekTo(pos);
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
				toFile(curFile - 1, 0, true);
				mp.seekTo(Math.max(0, mp.getDuration() - seekStep));
			} else {
				mp.seekTo(Math.max(0, cur - seekStep));
			}
		} else {
			if (cur + seekStep >= mp.getDuration()) {
				if (curFile < folder.files.length - 1) {
					toFile(curFile + 1, 0, true);
				}
			} else {
				mp.seekTo(cur + seekStep);
			}
		}
	}

	/*
	 * public void seekBack(int seekStep) { if (folders.size() == 0 || curFolder
	 * < 0) return;
	 * 
	 * if (!mp.isPlaying()) return;
	 * 
	 * int cur = mp.getCurrentPosition();
	 * 
	 * if (cur < seekStep && curFile != 0) { toFile(curFile - 1, 0, true);
	 * mp.seekTo(Math.max(0, mp.getDuration() - seekStep)); } else {
	 * mp.seekTo(Math.max(0, cur - seekStep)); } }
	 * 
	 * public void seekForward(int seekStep) { if (folders.size() == 0 ||
	 * curFolder < 0) return;
	 * 
	 * if (!mp.isPlaying()) return;
	 * 
	 * int dur = mp.getDuration(); int cur = mp.getCurrentPosition(); if (cur +
	 * seekStep < dur) { mp.seekTo(cur + seekStep); } }
	 */
	public void prev() {
		if (folders.size() == 0 || curFolder < 0)
			return;

		int cur = mp.getCurrentPosition();
		if (cur < 3000 && curFile > 0)
			toFile(curFile - 1, 0, true);
		restart(false);
		onChanged();
	}

	public void next(boolean forcePlay) {
		if (folders.size() == 0 || curFolder < 0)
			return;

		Folder folder = folders.get(curFolder);
		if (curFile >= folder.files.length - 1) {
			return;
		}
		toFile(curFile + 1, 0, forcePlay);
		onChanged();
	}

	private void restart(boolean forcePlay) {
		try {
			boolean playing = mp.isPlaying();
			if (playing) {
				mp.stop();
				onChanged();
			}
			mp.reset();
			setDataSource();
			initialized = true;
			mp.prepare();
			if (playing || forcePlay)
				mp.start();
		} catch (Exception e) {
		}
	}

	public void pause() {
		if (mp.isPlaying()) {
			mp.pause();
			onChanged();
		}
	}

	public void play_pause() {
		if (mp.isPlaying())
			mp.pause();
		else
			mp.start();
		onChanged();
	}

	public void close() {
		mp.release();
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
}
