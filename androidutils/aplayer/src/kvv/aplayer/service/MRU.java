package kvv.aplayer.service;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MRU {
	public final static int mruSize = 10;
	private SharedPreferences settings;

	public MRU(Context context) {
		settings = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void addMRU(String s) {
		List<String> mruFolders = getMRU();

		mruFolders.remove(s);
		mruFolders.add(0, s);

		if (mruFolders.size() > mruSize)
			mruFolders = new ArrayList<String>(mruFolders.subList(0, mruSize));

		setMRU(mruFolders);
	}

	public List<String> getMRU() {
		List<String> mruFolders = new ArrayList<String>();
		for (int i = 0; i < mruSize; i++) {
			String path = settings.getString("mru" + i, null);
			if (path != null)
				mruFolders.add(path);
		}
		return mruFolders;
	}

	private void setMRU(List<String> mruFolders) {
		for (int i = 0; i < mruSize; i++) {
			SharedPreferences.Editor editor = settings.edit();
			if (i < mruFolders.size())
				editor.putString("mru" + i, mruFolders.get(i));
			else
				editor.remove("mru" + i);
			editor.apply();
		}
	}
}
