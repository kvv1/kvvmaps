package kvv.aplayer.files;

import java.util.List;

import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.IAPService;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FilesAdapter extends ArrayAdapter<FileDescriptor> {

	private IAPService service;

	public int sel = -1;

	public FilesAdapter(Activity activity, IAPService service) {
		super(activity, 0, service.getFiles().files);
		this.service = service;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		List<String> badSongs = service.getBadSongs();

		TextView tv = (TextView) convertView;
		if (tv == null) {
			tv = new TextView(getContext());
			tv.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
			tv.setSingleLine(true);
		}

		tv.setText(getItem(position).name);

		if (position == sel)
			tv.setBackgroundColor(0xFFFFFF80);
		else if (sel < 0 && position == service.getFiles().curFile)
			tv.setBackgroundColor(0xFFFFFF80);
		// else if(badSongs.contains(getItem(position).path))
		// v.setBackgroundColor(0xFFFFE0E0);
		else
			tv.setBackgroundColor(0xFFFFFFFF);

		if (badSongs.contains(getItem(position).path))
			tv.setTextColor(0xFFFF4040);
		else
			tv.setTextColor(0xFF000000);

		return tv;
	}
}