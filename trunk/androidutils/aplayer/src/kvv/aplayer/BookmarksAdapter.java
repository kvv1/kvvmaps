package kvv.aplayer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BookmarksAdapter extends ArrayAdapter<Bookmark> {

	private Activity activity;

	public int sel = -1;

	public BookmarksAdapter(Activity activity, IAPService service) {
		super(activity, R.layout.folder_item, service.getBookmarks());
		this.activity = activity;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		// if (v == null) {
		LayoutInflater vi = activity.getLayoutInflater();
		// }

		v = vi.inflate(R.layout.bookmark_item, null);

		Bookmark bookmark = getItem(position);
		((TextView) v.findViewById(R.id.folder)).setText(bookmark.folder);
		((TextView) v.findViewById(R.id.track)).setText(bookmark.track);

		ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress);
		progressBar.setMax(bookmark.duration);
		progressBar.setProgress(bookmark.time);

		if (position == sel)
			v.setBackgroundColor(0xFFFFFF80);
		else
			v.setBackgroundColor(0xFFFFFFFF);

		return v;
	}
}