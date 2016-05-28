package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.R;
import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.Folder;
import kvv.aplayer.service.IAPService;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MRUDialog extends Dialog {

	private Handler handler = new Handler();

	public MRUDialog(Context context, final IAPService service, final ListView listView) {
		super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

		setContentView(R.layout.popup_panel);

		findViewById(R.id.undo).setOnClickListener(
				new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						service.undo();
						showUndoRedoPanel();
					}

				});

		findViewById(R.id.redo).setOnClickListener(
				new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						service.redo();
						showUndoRedoPanel();
					}
				});

		findViewById(R.id.random).setOnClickListener(
				new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Folder folder = service.getFolders().getFolder();
						service.setRandom(!folder.random);
						dismiss();
					}
				});

		final FileDescriptor file = service.getFiles().getFile();

		if (file != null) {
			Button dontlike = (Button) findViewById(R.id.badsong);
			final List<String> allBadSongs = service.getBadSongs();

			if (allBadSongs.contains(file.path))
				dontlike.setText("Like");
			else
				dontlike.setText("Don't like");

			dontlike.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (allBadSongs.contains(file.path))
						service.delBadSong(file.path);
					else {
						service.addBadSong(file.path);
						service.next();
					}
					listView.invalidateViews();
					dismiss();
				}
			});
		}

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mru);
		linearLayout.removeAllViews();

		Folder folder = service.getFolders().getFolder();

		List<String> mru = service.getMRU();
		for (final String s : mru) {
			if (folder != null && folder.path.equals(s))
				continue;

			Button b = new Button(context);
			b.setText(s);
			b.setSingleLine();
			b.setEllipsize(TruncateAt.START);
			b.setTextAppearance(context,
					android.R.style.TextAppearance_Medium);
			b.setTypeface(Typeface.DEFAULT_BOLD);
			b.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Folders folders = service.getFolders();
					int index = folders.getIndex(s);
					if (index >= 0) {
						service.toFolder(index);
						dismiss();
					}
				}
			});

			linearLayout.addView(b);
		}
		
		showUndoRedoPanel();
	}
	
	private void showUndoRedoPanel() {
		handler.removeCallbacksAndMessages(null);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				dismiss();
			}
		}, 5000);
	}
}
