package kvv.aplayer.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.Folder;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public abstract class Player0 extends Player {

	protected abstract List<String> getBadSongs();

	private Folders folders;
	private int curFile = 0;

	public Player0(final List<Folder> _folders) {
		this.folders = new Folders(_folders, -1);
		setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp1) {
				System.out.println("onCompletion");

				Integer next = getNext();
				if (next == null) {
					curFile = 0;
					playFile(folders.getFolder().filesToPlay.get(curFile).path,
							0, false);
				} else {
					toFile(next);
				}
			}
		});
	}

	public Folders getFolders() {
		return folders;
	}

	public Files getFiles() {
		Folder folder = folders.getFolder();
		if (folder == null)
			return new Files(Collections.<FileDescriptor> emptyList(), -1);

		return new Files(folder.filesToPlay, curFile);
	}

	public void toFolder(int folderIdx, int file, int curPos) {
		if (folders.curFolder == folderIdx)
			return;
		_toFolder(folderIdx, file, curPos);
	}

	private void _toFolder(int folderIdx, int file, int curPos) {
		Folder folder = folders.getFolder();
		if (folder != null)
			folder.filesToPlay = null;

		folders.curFolder = folderIdx;

		folder = folders.getFolder();

		folder.filesToPlay = getAllFiles(folder);
		if (folder.random) {
			Collections.shuffle(folder.filesToPlay);
			curPos = 0;
		}

		toFile(file, curPos);

		onChanged(OnChangedHint.FOLDER);
	}

	public void setRandom(boolean random) {
		Folder folder = folders.getFolder();
		if (folder == null)
			return;

		folder.random = random;

		onChanged(OnChangedHint.FOLDER);
		_toFolder(folders.curFolder, 0, 0);
	}

	private List<FileDescriptor> getAllFiles(Folder folder) {
		List<FileDescriptor> files = new ArrayList<FileDescriptor>();
		files.addAll(folder._files);
		for (int i = folders.curFolder + 1; i < folders.folders.size(); i++) {
			Folder f = folders.folders.get(i);
			if (f.indent <= folder.indent)
				break;
			files.addAll(f._files);
		}
		return files;
	}

	private void toFile(int idx, int pos) {
		Folder folder = folders.getFolder();
		if (folder == null)
			return;

		if (idx >= folder.filesToPlay.size())
			curFile = 0;
		else
			curFile = idx;

		if (folder.filesToPlay.size() > curFile)
			playFile(folder.filesToPlay.get(curFile).path, pos, true);
	}

	public void toFile(int idx) {
		toFile(idx, 0);
	}

	public void prev() {
		int cur = getCurrentPosition();
		Integer prev = getPrev();

		if (cur < 3000 && prev != null)
			toFile(prev);
		else
			seekTo(0);
	}

	private Integer getPrev() {
		Folder folder = folders.getFolder();
		if (folder == null)
			return null;
		return Files.skipBw(folder.filesToPlay, curFile - 1, getBadSongs());
	}

	private Integer getNext() {
		Folder folder = folders.getFolder();
		if (folder == null)
			return null;
		return Files.skipFw(folder.filesToPlay, curFile + 1, getBadSongs());
	}

	public boolean hasNext() {
		return getNext() != null;
	}

	public void next() {
		Integer next = getNext();
		if (next == null)
			return;
		toFile(next);
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
				Integer prev = getPrev();
				if (prev != null)
					toFile(prev, Math.max(0, getDuration() - seekStep));
			}
		} else {
			if (cur + seekStep < getDuration()) {
				seekTo(cur + seekStep);
			} else {
				Integer next = getNext();
				if (next != null)
					toFile(next);
			}
		}
	}
}
