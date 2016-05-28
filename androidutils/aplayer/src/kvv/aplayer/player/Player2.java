package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.service.Folder;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

public abstract class Player2 extends Player1 {
	class Saver implements Runnable {
		@Override
		public void run() {
			save();
			handler.postDelayed(this, 60000);
		}
	}

	private Saver saver;
	private Handler handler = new Handler();
	private SharedPreferences settings;

	public Player2(Context context, List<Folder> folders) {
		super(folders);
		settings = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void onChanged(OnChangedHint hint) {
		super.onChanged(hint);

		if (!isPlaying()) {
			handler.removeCallbacks(saver);
			saver = null;
		} else {
			if (saver == null) {
				saver = new Saver();
				handler.post(saver);
			}
		}
	}

	public void toFolder(int folderIdx) {
		save();

		Folder folder = getFolders().folders.get(folderIdx);
		int curFile = settings.getInt(folder.path + "|file", 0);
		int curPos = settings.getInt(folder.path + "|pos", 0);

		toFolder(folderIdx, curFile, curPos);
	}

	@Override
	public void setRandom(boolean random) {
		save();
		super.setRandom(random);
	}
	
	private void save() {
		Folder folder = getFolders().getFolder();

		if (folder == null)
			return;

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(folder.path + "|file", getFiles().curFile);
		editor.putInt(folder.path + "|pos", getCurrentPosition());
		editor.apply();
	}

}
