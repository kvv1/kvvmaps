package kvv.aplayer.folders;

import java.util.ArrayList;
import java.util.List;

import kvv.aplayer.R;
import kvv.aplayer.service.Folder;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FoldersAdapter extends ArrayAdapter<Object> {

	private Activity activity;
	private IAPService service;
	public int sel = -1;

	public FoldersAdapter(Activity activity, IAPService service) {
		super(activity, R.layout.folder_item, createItems(service));
		this.activity = activity;
		this.service = service;
	}

	private static List<Object> createItems(IAPService service) {
		List<Object> res = new ArrayList<Object>();
		List<String> mru = service.getMRU();
		res.addAll(mru);

		// if (service.getFolders().getFolder() != null)
		// res.remove(service.getFolders().getFolder().path);

		for (Folder folder : service.getFolders().folders)
			res.add(folder);

		return res;
	}

	@SuppressLint({ "ViewHolder", "InflateParams" })
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		// if (v == null) {
		LayoutInflater vi = activity.getLayoutInflater();
		// }

		v = vi.inflate(R.layout.folder_item, null);

		TextView tv = (TextView) v.findViewById(R.id.text);

		Object item = getItem(position);

		if (item instanceof Folder) {
			Folder folder = (Folder) item;
			tv.setText(folder.path.substring(folder.path.lastIndexOf("/") + 1));
			tv.setPadding(folder.indent * 20, 0, 0, 0);
		} else if (item instanceof String) {
			tv.setText((String) item);
			tv.setEllipsize(TruncateAt.START);
			tv.setTypeface(null, Typeface.BOLD);
			// tv.setTextAppearance(activity,
			// android.R.style.TextAppearance_Medium);
		}

		if (position == sel)
			v.setBackgroundColor(0xFFFFFF80);
		else if (sel < 0 && position == service.getFolders().curFolder + service.getMRU().size())
			v.setBackgroundColor(0xFFFFFF80);
		else
			v.setBackgroundColor(0xFFFFFFFF);

		return v;
	}
}