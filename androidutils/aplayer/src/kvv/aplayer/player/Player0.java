package kvv.aplayer.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.Folder;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public abstract class Player0 extends Player {

	private Folders folders;
	private int curFile = 0;

	public Player0(Context context, final List<Folder> _folders) {
		super(context);
		this.folders = new Folders(_folders, -1);
		setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp1) {
				System.out.println("onCompletion");

				Integer next = getNext();
				if (next == null) {
					Folder folder = folders.getFolder();
					if (folder.seed != null) {
						folder.seed = Shuffle.shuffle(folder.filesToPlay);
						for (PlayerListener listener : listeners)
							listener.folderChanged();
					}

					Integer idx = skipFw(folder.filesToPlay, 0);
					curFile = idx == null ? 0 : idx;

					playFile(folder.filesToPlay.get(curFile).path, 0, false);
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

	public void toFolder(int folderIdx, int file, int curPos, Long seed,
			boolean play) {
		Folder folder = folders.getFolder();
		if (folder != null)
			folder.filesToPlay = null;

		folders.curFolder = folderIdx;

		folder = folders.getFolder();

		folder.filesToPlay = getAllFiles();
		folder.seed = seed;
		if (folder.seed != null)
			Shuffle.shuffle(folder.filesToPlay, folder.seed);

		for (PlayerListener listener : listeners)
			listener.folderChanged();

		curFile = 0;

		toFile(file, curPos, play);
	}

	public void setRandom() {
		Folder folder = folders.getFolder();
		if (folder == null)
			return;

		folder.filesToPlay = getAllFiles();
		if (folder.seed == null) {
			folder.seed = Shuffle.shuffle(folder.filesToPlay);
		} else {
			folder.seed = null;
		}

		for (PlayerListener listener : listeners)
			listener.folderChanged();
		toFile(0, 0, true);
	}

	private List<FileDescriptor> getAllFiles() {
		Folder folder = folders.getFolder();
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

	private void toFile(int idx, int pos, boolean play) {
		Folder folder = folders.getFolder();
		if (folder == null)
			return;

		if (idx >= folder.filesToPlay.size())
			curFile = 0;
		else
			curFile = idx;

		if (folder.filesToPlay.size() > curFile)
			playFile(folder.filesToPlay.get(curFile).path, pos, play);
	}

	public void toFile(int idx) {
		toFile(idx, 0, true);
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
		return skipBw(folder.filesToPlay, curFile - 1);
	}

	private Integer getNext() {
		Folder folder = folders.getFolder();
		if (folder == null)
			return null;
		return skipFw(folder.filesToPlay, curFile + 1);
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

	private Integer skipFw(List<FileDescriptor> files, int pos) {
		for (int i = pos; i < files.size(); i++)
			if (!isBadSong(files.get(i).path))
				return i;
		return null;
	}

	private Integer skipBw(List<FileDescriptor> files, int pos) {
		for (int i = pos; i >= 0; i--)
			if (!isBadSong(files.get(i).path))
				return i;
		return null;
	}

	protected boolean isBadSong(String path) {
		return false;
	}

}
