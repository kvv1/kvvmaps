package kvv.aplayer;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.smartbean.androidutils.fragment.RLFragment;
import com.smartbean.androidutils.util.AsyncCallback;
import com.smartbean.androidutils.util.Utils;

public class FoldersSectionFragment extends RLFragment<APActivity, IAPService> {
	private boolean noSel;

	private final APServiceListener listener = new APServiceListener() {
		@Override
		public void onChanged() {
			if (conn.service != null) {
				int curFolder = conn.service.getCurrentFolder();
				if (curFolder < list.getCount()) {
					list.setSelection(curFolder);
					list.invalidateViews();
					if (!noSel)
						list.setSelection(curFolder);
				}
			}
		}

		@Override
		public void onBookmarksChanged() {
		}

		@Override
		public void onRandomChanged() {
		}
	};

	private ListView list;

	public FoldersSectionFragment() {
		super(APService.class);
	}

	@Override
	protected int getLayout() {
		return R.layout.fragment_folders;
	}

	private FoldersAdapter adapter;

	@Override
	protected void createUI(IAPService service) {
		list = (ListView) rootView.findViewById(R.id.list);
		adapter = new FoldersAdapter(getActivity(), service);
		list.setAdapter(adapter);
		service.addListener(listener);
		listener.onChanged();

		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView,
					View view, final int position, long id) {
				if (conn.service == null)
					return false;

				Utils.select(getActivity(), "", new String[] { "Play",
						"Play random" }, new AsyncCallback<Integer>() {
					@Override
					public void onSuccess(Integer res) {
						if (res == 0) {
							try {
								noSel = true;
								conn.service.toFolder(position);
							} finally {
								noSel = false;
							}
						}
						if (res == 1) {
							noSel = true;
							conn.service.toRandom(position);
							noSel = false;
						}
					}
				});

				// try {
				// noSel = true;
				// conn.service.toFolder(position);
				// } finally {
				// noSel = false;
				// }

				return false;
			}
		});
	}

	@Override
	public void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(listener);
		super.onDestroy();
	}

}
