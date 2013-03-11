package kvv.picturelist;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class PLActivity extends Activity {

	private static final File ROOT = new File("/sdcard/");

	private static final String[] fileTypes = { ".jpg" };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ListView list = (ListView) findViewById(R.id.PictureList);

		final List<File> files = new ArrayList<File>();

		fillFileList(ROOT, files);

		list.setAdapter(new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				File file = files.get(position);

				if (convertView instanceof ThumbLayout) {
					((ThumbLayout) convertView).set(file);
					return convertView;
				} else {
					return new ThumbLayout(PLActivity.this, file);
				}
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public Object getItem(int position) {
				return files.get(position);
			}

			@Override
			public int getCount() {
				return files.size();
			}
		});

	}

	private void fillFileList(File root, List<File> files) {
		File[] fileArray = root.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory())
					return true;
				for (String type : fileTypes)
					if (file.getName().toLowerCase().endsWith(type))
						return true;
				return false;
			}
		});

		if (fileArray != null) {
			for (File f : fileArray) {
				if (f.isDirectory()) {
					fillFileList(f, files);
				} else {
					files.add(f);
				}
			}
		}
	}
}