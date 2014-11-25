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
import android.media.audiofx.Equalizer;

public abstract class Player {

	protected abstract void onChanged();

	protected abstract void onRandomChanged();

	private void onChanged1() {
		onChanged();
	}

	private final MediaPlayer mp = new MediaPlayer();
	private final Compressor compr = new Compressor(mp);
	private Equalizer eq = new Equalizer(0, mp.getAudioSessionId());

	private boolean initialized;

	private List<Folder> folders;
	private int curFolder = -1;
	private int curFile = 0;

	short[] bandRange;
	short nBands;
	int maxGain;

	public Player(List<Folder> folders) {

		nBands = eq.getNumberOfBands();
		bandRange = eq.getBandLevelRange();
		maxGain = bandRange[1];

		System.out.println("min=" + bandRange[0] + " max=" + bandRange[1]);
		eq.setEnabled(true);
		setEq(0);

		compr.init();

		this.folders = folders;
		folders.add(new Folder("RANDOM", 0, new File[0]));

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
					restart(false);
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

		eq.setEnabled(true);
		compr.setEnabled(true);

		setGain(0);
		onChanged1();

	}

	private void setEq(int j) {
		if (eq.getEnabled())
			for (short i = 0; i < nBands; i++) {
				eq.setBandLevel(i, (short) j);
			}
	}

	public void setVolume(float v) {
		// mp.setVolume(v, v);
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
		onChanged1();
	}

	public void toFile(int idx) {
		toFile(idx, 0, true);
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

	private void restart(boolean forcePlay) {
		try {
			boolean playing = mp.isPlaying();
			if (playing) {
				mp.stop();
				onChanged1();
			}
			mp.reset();
			setDataSource();
			initialized = true;
			mp.prepare();
			compr.resetGain();
			if (playing || forcePlay)
				mp.start();
		} catch (Exception e) {
		}
	}

	public void pause() {
		if (mp.isPlaying()) {
			mp.pause();
			onChanged1();
		}
	}

	public void play_pause() {
		compr.resetGain();
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
		eq.release();
		compr.release();
		mp.release();
	}

	private int gain;

	public void setGain(int db) {
		float k = compr.getK();
		int comprDB = (int) (20 * Math.log10(1 / k));
		setEq(comprDB + db * 100);
		gain = db;
	}

	public void setCompr(boolean b) {
		compr.setAuto(b);
	}

	public int getGain() {
		return gain;
	}

	public boolean getCompr() {
		return compr.getAuto();
	}

	public void enVis() {
		boolean b = mp.isPlaying();
		System.out.println("*** " + b);
		compr.setEnabled(b);
	}

}
