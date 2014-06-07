package kvv.aplayer;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.smartbean.androidutils.fragment.RLFragment;

public class BookmarksSectionFragment extends
		RLFragment<APActivity, IAPService> {
	private ListView list;

	public BookmarksSectionFragment() {
		super(APService.class);
	}

	@Override
	protected int getLayout() {
		return R.layout.fragment_bookmarks;
	}

	private BookmarksAdapter adapter;

	private APServiceListener listener = new APServiceListener() {
		@Override
		public void onChanged() {
		}

		@Override
		public void onBookmarksChanged() {
			if (conn.service == null)
				return;
			adapter = new BookmarksAdapter(getActivity(), conn.service);
			list.setAdapter(adapter);
		}

		@Override
		public void onRandomChanged() {
		}
	};

	@Override
	protected void createUI(IAPService service) {
		list = (ListView) rootView.findViewById(R.id.list);

		service.addListener(listener);
		listener.onBookmarksChanged();

		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView,
					View view, int position, long id) {
				if (conn.service != null) {
					conn.service.toBookmark(conn.service.getBookmarks().get(
							position));
				}
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
