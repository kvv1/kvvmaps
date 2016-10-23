package kvv.aplayer.files;

import kvv.aplayer.APActivity;
import kvv.aplayer.R;
import kvv.aplayer.player.Files;
import kvv.aplayer.player.Player.PlayerAdapter;
import kvv.aplayer.player.Player.PlayerListener;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class FilesSectionFragmentList extends FilesSectionFragmentBase {

	private ListView listView;

	private Handler handler = new Handler();

	private Runnable buttonsRunnable = new Runnable1() {
		@Override
		public void run1() {
			clearButtons();
		}
	};

	private PlayerListener listener = new PlayerAdapter() {
		@Override
		public void folderChanged() {
			FilesAdapter adapter = new FilesAdapter(getActivity(), conn.service);
			listView.setAdapter(adapter);
		}

		@Override
		public void fileChanged() {
			clearButtons();
			Files files = conn.service.getFiles();
			listView.invalidateViews();
			listView.setSelection(files.curFile - 2);
		}
	};

	public FilesSectionFragmentList() {
		super(APService.class, R.layout.fragment_files_list);
	}

	private void restartButtonsTimer() {
		rootView.findViewById(R.id.goto1).setVisibility(View.VISIBLE);
		handler.removeCallbacks(buttonsRunnable);
		handler.postDelayed(buttonsRunnable, APActivity.BUTTONS_DELAY);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void createUI(final IAPService service) {
		super.createUI(service);

		rootView.findViewById(R.id.goto1).setVisibility(View.GONE);

		listView = (ListView) rootView.findViewById(R.id.list);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				restartButtonsTimer();
				FilesAdapter adapter = (FilesAdapter) listView.getAdapter();
				if (adapter != null) {
					adapter.sel = position;
					listView.invalidateViews();
				}
			}
		});

		((Button) rootView.findViewById(R.id.goto1))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						FilesAdapter adapter = (FilesAdapter) listView
								.getAdapter();
						if (adapter != null && adapter.sel >= 0
								&& conn.service != null) {
							conn.service.toFile(adapter.sel);
						}
					}
				});

		service.addListener(listener);
		listener.folderChanged();
		listener.fileChanged();
	}

	@Override
	public void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(listener);
		super.onDestroy();
	}

	private void clearButtons() {
		handler.removeCallbacks(buttonsRunnable);
		if (rootView != null) {
			rootView.findViewById(R.id.goto1).setVisibility(View.GONE);
		}
		FilesAdapter adapter = (FilesAdapter) listView.getAdapter();
		if (adapter != null) {
			adapter.sel = -1;
			listView.invalidateViews();
		}
	}

	@Override
	public void onPause() {
		handler.removeCallbacksAndMessages(null);
		super.onPause();
	}

}
