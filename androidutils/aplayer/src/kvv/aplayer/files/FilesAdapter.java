package kvv.aplayer.files;

import java.util.List;
import kvv.aplayer.R;
import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FilesAdapter extends ArrayAdapter<FileDescriptor> {

	private Activity activity;
	private IAPService service;

	public int sel = -1;

	public FilesAdapter(Activity activity, IAPService service) {
		super(activity, R.layout.folder_item, service.getFiles().files);
		this.activity = activity;
		this.service = service;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		List<String> badSongs = service.getBadSongs();

		
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = activity.getLayoutInflater();
			v = vi.inflate(R.layout.folder_item, null);
		}

		TextView tv = (TextView) v.findViewById(R.id.text);
		tv.setText(getItem(position).name);

		if (position == sel)
			v.setBackgroundColor(0xFFFFFF80);
		else if (sel < 0 && position == service.getFiles().curFile)
			v.setBackgroundColor(0xFFFFFF80);
		else if(badSongs.contains(getItem(position).path))
			v.setBackgroundColor(0xFFFFE0E0);
		else
			v.setBackgroundColor(0xFFFFFFFF);

		return v;
	}
}