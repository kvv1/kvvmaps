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

public class BookmarksSectionFragment extends
		RLFragment<APActivity, IAPService> {
	private ListView list;

	private Handler handler = new Handler();

	public BookmarksSectionFragment() {
		super(APService.class);
	}

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
		BookmarksAdapter adapter = (BookmarksAdapter) list.getAdapter();
		if (adapter != null) {
			adapter.sel = -1;
			list.invalidateViews();
		}
	}

	@Override
	protected int getLayout() {
		return R.layout.fragment_bookmarks;
	}

	private BookmarksAdapter adapter;

	private APServiceListener listener = new APServiceListenerAdapter() {

		@Override
		public void onBookmarksChanged() {
			if (conn.service == null)
				return;
			adapter = new BookmarksAdapter(getActivity(), conn.service);
			list.setAdapter(adapter);
		}

	};

	@Override
	protected void createUI(IAPService service) {
		list = (ListView) rootView.findViewById(R.id.list);

		service.addListener(listener);
		listener.onBookmarksChanged();

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					final int position, long id) {
				rootView.findViewById(R.id.buttons).setVisibility(View.VISIBLE);
				handler.removeCallbacks(gotoRunnable);
				handler.postDelayed(gotoRunnable, APActivity.BUTTONS_DELAY);
				BookmarksAdapter adapter = (BookmarksAdapter) list.getAdapter();
				if (adapter != null) {
					adapter.sel = position;
					list.invalidateViews();
				}
			}
		});

		((Button) rootView.findViewById(R.id.goto1))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						BookmarksAdapter adapter = (BookmarksAdapter) list
								.getAdapter();
						if (adapter != null && adapter.sel >= 0
								&& conn.service != null) {
							conn.service.toBookmark(conn.service.getBookmarks()
									.get(adapter.sel));
							APActivity activity = (APActivity) getActivity();
							ViewPager pager = (ViewPager) activity
									.findViewById(activity.getPagerId());
							pager.setCurrentItem(0, true);
							clearGoto();
						}
					}
				});

		((Button) rootView.findViewById(R.id.del))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						BookmarksAdapter adapter = (BookmarksAdapter) list
								.getAdapter();
						if (adapter != null && adapter.sel >= 0
								&& conn.service != null) {
							conn.service.delBookmark(conn.service
									.getBookmarks().get(adapter.sel));
							clearGoto();
						}
					}
				});

		clearGoto();
	}

	@Override
	public void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(listener);
		super.onDestroy();
	}

}
