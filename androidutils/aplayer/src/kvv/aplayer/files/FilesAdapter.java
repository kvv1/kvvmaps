package kvv.aplayer.files;

import kvv.aplayer.R;
import kvv.aplayer.service.File1;
import kvv.aplayer.service.IAPService;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FilesAdapter extends ArrayAdapter<File1> {

	private Activity activity;
	private IAPService service;

	public int sel = -1;

	public FilesAdapter(Activity activity, IAPService service) {
		super(activity, R.layout.folder_item, service.getFiles());
		this.activity = activity;
		this.service = service;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = activity.getLayoutInflater();
			v = vi.inflate(R.layout.folder_item, null);
		}

		TextView tv = (TextView) v.findViewById(R.id.text);
		tv.setText(getItem(position).name);

		//System.out.print("%");
		
		if (position == sel)
			v.setBackgroundColor(0xFFFFFF80);
		else if (sel < 0 && position == service.getFile())
			v.setBackgroundColor(0xFFFFFF80);
		else
			v.setBackgroundColor(0xFFFFFFFF);

		return v;
	}
}