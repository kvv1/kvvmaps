package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.service.Folder;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

public abstract class Player2 extends Player1 {
	private Runnable saver = new Runnable() {
		@Override
		public void run() {
			save();
			handler.postDelayed(this, 60000);
		}
	};

	private Handler handler = new Handler();
	private SharedPreferences settings;

	public Player2(Context context, List<Folder> folders) {
		super(folders);
		settings = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void onChanged(OnChangedHint hint) {
		super.onChanged(hint);
		if (hint == OnChangedHint.FILE || hint == OnChangedHint.STATE) {
			handler.removeCallbacks(saver);
			if (isPlaying())
				handler.post(saver);
		}
	}

	public void toFolder(int folderIdx) {
		if (getFolders().curFolder == folderIdx)
			return;

		Folder folder = getFolders().folders.get(folderIdx);
		int curFile = settings.getInt(folder.path + "|file", 0);
		int curPos = settings.getInt(folder.path + "|pos", 0);
		Long seed = null;
		if (settings.contains(folder.path + "|seed"))
			seed = settings.getLong(folder.path + "|seed", 0);

		toFolder(folderIdx, curFile, curPos, seed);
	}

	@Override
	public void toFolder(int folderIdx, int file, int curPos, Long seed) {
		if (getFolders().curFolder != folderIdx)
			save();
		super.toFolder(folderIdx, file, curPos, seed);
	}

	// @Override
	// public void setRandom() {
	// save();
	// super.setRandom();
	// }

	private void save() {
		Folder folder = getFolders().getFolder();

		if (folder == null)
			return;

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(folder.path + "|file", getFiles().curFile);
		editor.putInt(folder.path + "|pos", getCurrentPosition());

		if (folder.seed != null)
			editor.putLong(folder.path + "|seed", folder.seed);
		else
			editor.remove(folder.path + "|seed");

		editor.putString("lastFolder", folder.path);
		editor.putBoolean("lastState", isPlaying());

		editor.apply();

		System.out.println("SAVED");
	}

	public void init() {
		String path = settings.getString("lastFolder", null);
		boolean state = settings.getBoolean("lastState", false);
		if (path != null && state) {
			int folderIdx = getFolders().getIndex(path);
			if (folderIdx >= 0)
				toFolder(folderIdx);
		}

	}

}
