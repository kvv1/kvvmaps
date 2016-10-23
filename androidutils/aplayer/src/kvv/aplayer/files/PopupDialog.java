package kvv.aplayer.files;

import java.util.List;
import kvv.aplayer.R;
import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

@SuppressLint("InlinedApi")
public class PopupDialog extends Dialog {

	private Handler handler = new Handler();

	public PopupDialog(Context context, final IAPService service) {
		super(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
		setCanceledOnTouchOutside(true);
		
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
						service.setRandom();
						dismiss();
					}
				});

		findViewById(R.id.home).setOnClickListener(
				new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						service.toFile(0);
						dismiss();
					}
				});

		findViewById(R.id.end).setOnClickListener(
				new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (service.getFiles().files.size() > 0)
							service.toFile(service.getFiles().files.size() - 1);
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
				dontlike.setText("Dislike");

			dontlike.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (allBadSongs.contains(file.path))
						service.delBadSong(file.path);
					else {
						service.addBadSong(file.path);
						service.next();
					}
					dismiss();
				}
			});
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

	@Override
	public void dismiss() {
		handler.removeCallbacksAndMessages(null);
		super.dismiss();
	}
}
