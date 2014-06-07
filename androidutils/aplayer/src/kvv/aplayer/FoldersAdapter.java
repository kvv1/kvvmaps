package kvv.aplayer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FoldersAdapter extends ArrayAdapter<Folder> {

	private Activity activity;
	private IAPService service;

	public FoldersAdapter(Activity activity, IAPService service) {
		super(activity, R.layout.folder_item, service.getFolders());
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

		Folder folder = getItem(position);

		TextView tv = (TextView) v.findViewById(R.id.text);
		tv.setText(folder.shortName);
		tv.setPadding(folder.indent * 20, 0, 0, 0);

		v.setBackgroundColor(position == service.getCurrentFolder() ? 0xFFFFFF80
				: 0xFFFFFFFF);

		return v;
	}
}