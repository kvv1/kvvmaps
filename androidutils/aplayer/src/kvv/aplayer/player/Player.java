package kvv.aplayer.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kvv.aplayer.service.File1;
import kvv.aplayer.service.Folder;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;

public abstract class Player {

	protected abstract void onChanged(OnChangedHint hint);

	public enum OnChangedHint {
		FOLDER, FILE, POSITION
	}

	private void onChanged1(OnChangedHint hint) {
		onChanged(hint);
	}

	protected final MediaPlayer mp = new MediaPlayer();

	private List<Folder> folders;
	private int curFolder = 0;
	private int curFile = 0;

	private boolean prepared;

	public Player(List<Folder> folders) {

		this.folders = folders;
		folders.add(new Folder("RANDOM", 0, new File1[0]));

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
					boolean isPlaying = mp.isPlaying();
					System.out.println("end of folder " + folder.displayName
							+ " " + isPlaying);
					reload();
					onChanged1(OnChangedHint.FILE);
					return;
				}

				next();// onChanged1();
			}
		});

		mp.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				onChanged1(OnChangedHint.FOLDER);
				prepared = false;
				return false;
			}
		});

		mp.setOnInfoListener(new OnInfoListener() {
			@Override
			public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
				onChanged1(OnChangedHint.FOLDER);
				return false;
			}
		});

		onChanged1(OnChangedHint.FOLDER);
	}

	private void toFile(int idx, int pos) {
		if (folders.size() == 0 || curFolder < 0)
			return;

		Folder folder = folders.get(curFolder);
		if (idx >= folder.files.length)
			curFile = 0;
		else
			curFile = idx;

		reload();
		mp.start();
		mp.seekTo(pos);
	}

	private void reload() {
		try {
			boolean playing = mp.isPlaying();

			if (playing)
				mp.stop();

			mp.reset();
			prepared = false;

			if (folders.size() == 0 || curFolder < 0)
				return;

			if (curFolder >= folders.size())
				return;

			Folder folder = folders.get(curFolder);

			if (curFile >= folder.files.length)
				return;

			mp.setDataSource(folder.files[curFile].path);
			mp.prepare();

			prepared = true;

			resetGain();
		} catch (Exception e) {
			prepared = false;
		}
	}

	public void makeRandom(int folderIdx) {
		if (folders.size() == 0)
			return;

		Folder folder = folders.get(folderIdx);

		List<File1> files = new ArrayList<File1>();
		files.addAll(Arrays.asList(folder.files));
		for (int i = folderIdx + 1; i < folders.size(); i++) {
			Folder f = folders.get(i);
			if (f.indent <= folder.indent)
				break;
			files.addAll(Arrays.asList(f.files));
		}

		Collections.shuffle(files);

		Folder randFolder = folders.get(folders.size() - 1);

		randFolder.files = files.toArray(new File1[0]);
		randFolder.displayName = folder.displayName + " RND";

		onChanged1(OnChangedHint.FOLDER);
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
		toFile(file, curPos);
		onChanged1(OnChangedHint.FOLDER);
	}

	public void toFile(int idx) {
		toFile(idx, 0);
		onChanged1(OnChangedHint.FILE);
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
				onChanged1(OnChangedHint.FILE);
			} else {
				mp.seekTo(Math.max(0, cur - seekStep));
				onChanged1(OnChangedHint.POSITION);
			}
		} else {
			if (cur + seekStep >= mp.getDuration()) {
				if (curFile < folder.files.length - 1) {
					toFile(curFile + 1);
					onChanged1(OnChangedHint.FILE);
				}
			} else {
				mp.seekTo(cur + seekStep);
				onChanged1(OnChangedHint.POSITION);
			}
		}
	}

	public void prev() {
		if (folders.size() == 0 || curFolder < 0)
			return;

		int cur = mp.getCurrentPosition();
		if (cur < 3000 && curFile > 0) {
			toFile(curFile - 1, 0);
			onChanged1(OnChangedHint.FILE);
		} else {
			mp.seekTo(0);
			onChanged1(OnChangedHint.POSITION);
		}
	}

	public void next() {
		if (folders.size() == 0 || curFolder < 0)
			return;

		Folder folder = folders.get(curFolder);
		if (curFile >= folder.files.length - 1) {
			return;
		}
		toFile(curFile + 1, 0);
		onChanged1(OnChangedHint.FILE);
	}

	public void pause() {
		if (mp.isPlaying()) {
			mp.pause();
			onChanged1(OnChangedHint.POSITION);
		}
	}

	public void play_pause() {
		resetGain();
		if (mp.isPlaying())
			mp.pause();
		else
			mp.start();
		onChanged1(OnChangedHint.POSITION);
	}

	public int getDuration() {
		System.out.println("getDuration()");
		if (!prepared)
			return 1;
		return mp.getDuration();
	}

	public int getCurrentPosition() {
		System.out.println("getCurrentPosition()");
		if (!prepared)
			return 0;
		return mp.getCurrentPosition();
	}

	public int getFile() {
		System.out.println("getFile()");
		if (!prepared)
			return 0;
		return curFile;
	}

	public boolean isPlaying() {
		System.out.println("isPlaying()");
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
