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

		v.setBackgroundColor(position == service.getFile() ? 0xFFFFFF80
				: 0xFFFFFFFF);

		return v;
	}
}