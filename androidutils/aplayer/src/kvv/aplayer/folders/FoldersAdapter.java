package kvv.aplayer.folders;

import java.util.ArrayList;
import java.util.List;

import kvv.aplayer.service.Folder;
import kvv.aplayer.service.IAPService;
import android.app.Activity;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FoldersAdapter extends ArrayAdapter<Object> {

	private IAPService service;
	public int sel = -1;

	public FoldersAdapter(Activity activity, IAPService service) {
		super(activity, 0, createItems(service));
		this.service = service;
	}

	private static List<Object> createItems(IAPService service) {
		List<Object> res = new ArrayList<Object>();
		List<String> mru = service.getMRU();
		res.addAll(mru);

		for (Folder folder : service.getFolders().folders)
			res.add(folder);

		return res;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv = (TextView) convertView;
		if (tv == null) {
			tv = new TextView(getContext());
			tv.setTextAppearance(getContext(),
					android.R.style.TextAppearance_Large);
			tv.setSingleLine(true);
		}

		Object item = getItem(position);

		tv.setPadding(0, 0, 0, 0);
		tv.setEllipsize(TruncateAt.START);
		tv.setTypeface(null, Typeface.NORMAL);

		if (item instanceof Folder) {
			Folder folder = (Folder) item;
			tv.setText(folder.path.substring(folder.path.lastIndexOf("/") + 1));
			tv.setPadding(folder.indent * 20, 0, 0, 0);
		} else if (item instanceof String) {
			tv.setText((String) item);
			tv.setTypeface(null, Typeface.BOLD);
		}

		if (position == sel)
			tv.setBackgroundColor(0xFFFFFF80);
		else if (sel < 0
				&& position == service.getFolders().curFolder
						+ service.getMRU().size())
			tv.setBackgroundColor(0xFFFFFF80);
		else
			tv.setBackgroundColor(0xFFFFFFFF);

		return tv;
	}
}