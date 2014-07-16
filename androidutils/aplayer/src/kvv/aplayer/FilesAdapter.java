package kvv.aplayer;

import java.io.File;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FilesAdapter extends ArrayAdapter<File> {

	private Activity activity;
	private IAPService service;

	public int sel = -1;

	public FilesAdapter(Activity activity, IAPService service) {
		super(activity, R.layout.folder_item, service.getFolders().get(
				service.getCurrentFolder()).files);
		this.activity = activity;
		this.service = service;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		// if (v == null) {
		LayoutInflater vi = activity.getLayoutInflater();
		// }

		v = vi.inflate(R.layout.folder_item, null);

		File file = getItem(position);

		TextView tv = (TextView) v.findViewById(R.id.text);
		tv.setText(file.getName());

		if (position == sel)
			v.setBackgroundColor(0xFFFFFF80);
		else if (sel < 0 && position == service.getFile())
			v.setBackgroundColor(0xFFFFFF80);
		else
			v.setBackgroundColor(0xFFFFFFFF);

		return v;
	}
}