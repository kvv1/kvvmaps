package kvv.aplayer.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kvv.aplayer.service.File1;
import kvv.aplayer.service.Folder;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public abstract class Player0 extends Player {

	private List<Folder> folders;
	private int curFolder = 0;
	private int curFile = 0;

	public Player0(final List<Folder> folders) {
		this.folders = folders;
		folders.add(new Folder("RANDOM", 0, new File1[0]));

		setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp1) {
				System.out.println("onCompletion");

				String path = nextFile();
				if (path == null) {
					curFile = 0;
					Folder folder = folders.get(curFolder);
					if (folder.files.length > 0)
						toFile(folder.files[curFile].path, 0, false);
				} else {
					toFile(path, 0, true);
				}
			}
		});
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
		onChanged(OnChangedHint.FOLDER);
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

		onChanged(OnChangedHint.FOLDER);
		toFolder(folders.size() - 1, 0, 0);
	}

	private void toFile(int idx, int pos) {
		if (folders.size() == 0 || curFolder < 0)
			return;

		Folder folder = folders.get(curFolder);
		if (idx >= folder.files.length)
			curFile = 0;
		else
			curFile = idx;

		if (folder.files.length > curFile)
			toFile(folder.files[curFile].path, pos, true);
	}

	public void toFile(int idx) {
		toFile(idx, 0);
	}

	public void prev() {
		int cur = getCurrentPosition();
		if (cur < 3000 && curFile > 0) {
			toFile(curFile - 1);
		} else {
			seekTo(0);
		}
	}

	public void next() {
		if (folders.size() == 0 || curFolder < 0)
			return;

		Folder folder = folders.get(curFolder);
		if (curFile >= folder.files.length - 1) {
			return;
		}
		toFile(curFile + 1);
	}

	public void seek(int seekStep) {
		if (!isPlaying())
			return;

		int cur = getCurrentPosition();

		if (seekStep < 0) {
			seekStep = -seekStep;
			if (cur > seekStep) {
				seekTo(Math.max(0, cur - seekStep));
			} else {
				String path = prevFile();
				if (path != null)
					toFile(path, Math.max(0, getDuration() - seekStep), true);
			}
		} else {
			if (cur + seekStep < getDuration()) {
				seekTo(cur + seekStep);
			} else {
				String path = nextFile();
				if (path != null)
					toFile(path, 0, true);
			}
		}
	}

	public int getFile() {
		return curFile;
	}

	private String prevFile() {
		if (curFile <= 0)
			return null;
		curFile--;
		Folder folder = folders.get(curFolder);
		return folder.files[curFile].path;
	}

	private String nextFile() {
		Folder folder = folders.get(curFolder);
		if (curFile >= folder.files.length - 1)
			return null;
		curFile++;
		return folder.files[curFile].path;
	}

}
