package kvv.aplayer.player;

import java.util.LinkedList;
import java.util.List;

import kvv.aplayer.service.Folder;
import android.content.Context;

public class PlayerUndoRedo extends PlayerMRU {

	static class UndoItem {
		int folder;
		int file;
		int pos;
		Long seed;

		public UndoItem(int folder, int file, int pos, Long seed) {
			this.folder = folder;
			this.file = file;
			this.pos = pos;
			this.seed = seed;
		}
	}

	private LinkedList<UndoItem> undoList = new LinkedList<UndoItem>();
	private LinkedList<UndoItem> redoList = new LinkedList<UndoItem>();

	public PlayerUndoRedo(Context context, List<Folder> folders) {
		super(context, folders);
	}

	public void redo() {
		if (redoList.isEmpty())
			return;
		UndoItem item = redoList.removeLast();
		undoList.add(createUndoItem());
		toFolder(item.folder, item.file, item.pos, item.seed);
	}

	public void undo() {
		if (undoList.isEmpty())
			return;
		UndoItem item = undoList.removeLast();
		redoList.add(createUndoItem());
		toFolder(item.folder, item.file, item.pos, item.seed);
	}

	private UndoItem createUndoItem() {
		return new UndoItem(getFolders().curFolder, getFiles().curFile,
				getCurrentPosition(), getFolders().getFolder().seed);
	}

	private void storeUndo() {
		if (getFolders().curFolder < 0)
			return;
		undoList.add(createUndoItem());
		redoList.clear();
	}

	@Override
	public void toFolder(int folderIdx) {
		storeUndo();
		super.toFolder(folderIdx);
	}

	@Override
	public void setRandom() {
		storeUndo();
		super.setRandom();
	}

	@Override
	public void toFile(int idx) {
		storeUndo();
		super.toFile(idx);
	}

	@Override
	public void prev() {
		storeUndo();
		super.prev();
	}

	@Override
	public void next() {
		storeUndo();
		super.next();
	}

	@Override
	public void seekTo(int pos) {
		storeUndo();
		super.seekTo(pos);
	}
}
