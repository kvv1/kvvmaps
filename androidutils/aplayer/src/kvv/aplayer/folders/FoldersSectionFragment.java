package kvv.aplayer.folders;

import com.smartbean.androidutils.fragment.FragmentX;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.player.Player.PlayerAdapter;
import kvv.aplayer.player.Player.PlayerListener;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.Folder;
import kvv.aplayer.service.IAPService;

@SuppressLint("NewApi")
public class FoldersSectionFragment extends FragmentX<APActivity, IAPService> {
	private Handler handler = new Handler();

	private final PlayerListener listener = new PlayerAdapter() {
		@Override
		public void folderListChanged() {
			clearGoto();
			list.setAdapter(new FoldersAdapter(getActivity(), conn.service));
		}

		@Override
		public void folderChanged() {
			clearGoto();
		}

		@Override
		public void fileChanged() {
			clearGoto();
		}
	};

	private ListView list;

	public FoldersSectionFragment() {
		super(APService.class, R.layout.fragment_folders);
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
		FoldersAdapter adapter = (FoldersAdapter) list.getAdapter();
		if (adapter != null) {
			adapter.sel = -1;
			list.invalidateViews();
		}
	}

	@Override
	protected void createUI(final IAPService service) {
		System.out.println("createUI");
		list = (ListView) rootView.findViewById(R.id.list);
		service.addListener(listener);
		listener.folderListChanged();

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
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

		((Button) rootView.findViewById(R.id.goto1)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				FoldersAdapter adapter = (FoldersAdapter) list.getAdapter();
				if (adapter != null && adapter.sel >= 0 && conn.service != null) {
					Object item = adapter.getItem(adapter.sel);

					String path;
					if (item instanceof Folder)
						path = ((Folder) item).path;
					else
						path = (String) item;

					int idx = conn.service.getFolders().getIndex(path);
					conn.service.toFolder(idx);

					getActivity1().selectMainPage();
				}
			}
		});

		((Button) rootView.findViewById(R.id.home)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				list.setSelection(0);
			}
		});

		((Button) rootView.findViewById(R.id.cur)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (conn.service != null)
					list.setSelection(conn.service.getFolders().curFolder + conn.service.getMRU().size() - 5);
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
