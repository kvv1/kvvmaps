package kvv.aplayer.folders;

import kvv.aplayer.R;
import kvv.aplayer.service.Folder;
import kvv.aplayer.service.IAPService;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FoldersAdapter extends ArrayAdapter<Folder> {

	private Activity activity;
	private IAPService service;
	public int sel;

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

		if (position == sel)
			v.setBackgroundColor(0xFFFFFF80);
		else if (sel < 0 && position == service.getCurrentFolder())
			v.setBackgroundColor(0xFFFFFF80);
		else
			v.setBackgroundColor(0xFFFFFFFF);

		return v;
	}
}