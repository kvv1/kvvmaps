package kvv.aplayer;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BookmarksAdapter extends ArrayAdapter<Bookmark> {

	private Activity activity;
	private IAPService service;
	private List<Folder> folders;

	public BookmarksAdapter(Activity activity, IAPService service) {
		super(activity, R.layout.folder_item, service.getBookmarks());
		this.activity = activity;
		this.service = service;
		this.folders = service.getFolders();
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
		((TextView) v.findViewById(R.id.time)).setText(bookmark.time + "");

		return v;
	}
}