package kvv.aplayer;

import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.smartbean.androidutils.fragment.RLFragment;

public class FoldersSectionFragment extends RLFragment<APActivity, IAPService> {
	private boolean noSel;

	private Handler handler = new Handler();

	private final APServiceListener listener = new APServiceListenerAdapter() {
		@Override
		public void onChanged() {
			if (conn.service == null)
				return;

			clearGoto();

			int curFolder = conn.service.getCurrentFolder();
			if (curFolder < list.getCount()) {
				list.invalidateViews();
				if (!noSel)
					list.setSelection(curFolder - 2);
			}

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

	private Runnable gotoRunnable = new Runnable() {
		@Override
		public void run() {
			if (rootView != null) {
				clearGoto();
			}
		}
	};

	private void clearGoto() {
		handler.removeCallbacks(gotoRunnable);
		rootView.findViewById(R.id.buttons).setVisibility(View.GONE);
		FoldersAdapter adapter = (FoldersAdapter) list.getAdapter();
		if (adapter != null) {
			adapter.sel = -1;
			list.invalidateViews();
		}
	}

	@Override
	protected void createUI(IAPService service) {
		list = (ListView) rootView.findViewById(R.id.list);
		adapter = new FoldersAdapter(getActivity(), service);
		list.setAdapter(adapter);
		service.addListener(listener);
		listener.onChanged();

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					final int position, long id) {
				rootView.findViewById(R.id.buttons).setVisibility(View.VISIBLE);
				handler.removeCallbacks(gotoRunnable);
				handler.postDelayed(gotoRunnable, APActivity.BUTTONS_DELAY);
				FoldersAdapter adapter = (FoldersAdapter) list.getAdapter();
				if (adapter != null) {
					adapter.sel = position;
					list.invalidateViews();
				}
			}
		});

		// list.setOnItemLongClickListener(new OnItemLongClickListener() {
		// @Override
		// public boolean onItemLongClick(AdapterView<?> adapterView, View view,
		// final int position, long id) {
		// new AlertDialog.Builder(getActivity())
		// .setMessage(
		// "Play random?")
		// .setPositiveButton("OK",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int which) {
		// }
		// }).show();
		// return false;
		// }
		// });

		((Button) rootView.findViewById(R.id.goto1))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						FoldersAdapter adapter = (FoldersAdapter) list
								.getAdapter();
						if (adapter != null && adapter.sel >= 0
								&& conn.service != null) {
							try {
								noSel = true;
								conn.service.toFolder(adapter.sel);
								APActivity activity = (APActivity) getActivity();
								ViewPager pager = (ViewPager) activity
										.findViewById(activity.getPagerId());
								pager.setCurrentItem(0, true);
							} finally {
								noSel = false;
							}
						}
					}
				});

		((Button) rootView.findViewById(R.id.random))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						FoldersAdapter adapter = (FoldersAdapter) list
								.getAdapter();
						if (adapter != null && adapter.sel >= 0
								&& conn.service != null) {
							conn.service.toRandom(adapter.sel);
							APActivity activity = (APActivity) getActivity();
							ViewPager pager = (ViewPager) activity
									.findViewById(activity.getPagerId());
							pager.setCurrentItem(0, true);
						}
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
