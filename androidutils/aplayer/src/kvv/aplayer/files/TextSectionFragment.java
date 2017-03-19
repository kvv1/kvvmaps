package kvv.aplayer.files;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import kvv.aplayer.R;
import kvv.aplayer.player.Files;
import kvv.aplayer.player.Player.PlayerAdapter;
import kvv.aplayer.player.Player.PlayerListener;
import kvv.aplayer.service.APService;
import kvv.aplayer.service.FileDescriptor;
import kvv.aplayer.service.IAPService;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.smartbean.androidutils.util.AsyncCallback;

public class TextSectionFragment extends FilesSectionFragmentBase {

	private static final int STEP = 1000;

	private int lineCnt;
	private String lastTextPath;
	private Translator translator;

	@Override
	protected void onProgress() {
		super.onProgress();
		ScrollView sv = (ScrollView) rootView.findViewById(R.id.scroll);
		if (conn.service != null && conn.service.isPlaying() && sv != null) {
			int scrollY = sv.getScrollY();
			sv.scrollTo(0, scrollY + (int) dScroll(STEP));
		}
	}

	private PlayerListener listener = new PlayerAdapter() {

		@SuppressLint("InlinedApi")
		public void fileChanged() {
			System.out.println("FILE CHANGED");
			Files files = conn.service.getFiles();

			if (files == null || files.curFile < 0)
				return;

			System.out.println("OK");

			FileDescriptor fileDescriptor = files.files.get(files.curFile);
			String path = fileDescriptor.path;

			String textPath = path.substring(0, path.lastIndexOf('.')) + ".txt";

			if (textPath.equals(lastTextPath))
				return;
			lastTextPath = textPath;

			LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.lll);
			ScrollView sv = (ScrollView) rootView.findViewById(R.id.scroll);
			sv.scrollTo(0, 0);
			ll.removeAllViews();

			final List<String> lines = new ArrayList<String>();

			BufferedReader rd = null;
			try {
				rd = new BufferedReader(new InputStreamReader(
						new FileInputStream(textPath), "utf8"));
				String line;
				while ((line = rd.readLine()) != null) {
					lines.add(line);
				}
			} catch (Exception e) {
			} finally {
				if (rd != null)
					try {
						rd.close();
					} catch (IOException e) {
					}
			}

			lineCnt = lines.size();

			lines.add(0, "");
			lines.add(
					0,
					path.substring(0, path.lastIndexOf('.')).substring(
							path.lastIndexOf('/') + 1));

			for (int i = 0; i < 3; i++)
				lines.add(0, "");

			for (int i = 0; i < 5; i++)
				lines.add("");

			for (final String str : lines) {
				TextView tv = new TextView(getActivity());
				tv.setText(str);
				tv.setTextAppearance(getActivity(),
						android.R.style.TextAppearance_Large);
				tv.setSingleLine(true);
				ll.addView(tv);
				tv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						translator.translate(str, new AsyncCallback<String>() {
							@Override
							public void onSuccess(String res) {
								if(res == null)
									res = "ERROR";
								Dialog dialog = new Dialog(
										getActivity(),
										android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
								dialog.setContentView(R.layout.translate_panel);
								((TextView) (dialog.findViewById(R.id.textFrom)))
										.setText(str);
								((TextView) (dialog.findViewById(R.id.textTo)))
										.setText(res);
								dialog.setCanceledOnTouchOutside(true);
								dialog.show();
							}
						});
					}

				});
			}

		}
	};

	public TextSectionFragment() {
		super(APService.class, R.layout.fragment_text);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		translator = new Translator(getActivity());
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void createUI(final IAPService service) {
		System.out.println("CREATE UI");
		
		lastTextPath = null;
		
		super.createUI(service);

		service.addListener(listener);

		listener.fileChanged();

		Button sync = (Button) rootView.findViewById(R.id.syncText);
		sync.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ScrollView sv = (ScrollView) rootView.findViewById(R.id.scroll);
				sv.scrollTo(0, calcScrollY());
			}
		});
	}

	@Override
	public void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(listener);
		super.onDestroy();
	}

	private float dScroll(int ms) {
		LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.lll);
		if (ll.getChildCount() <= 0)
			return 0;
		View itemAtPosition = ll.getChildAt(0);
		if (itemAtPosition == null)
			return 0;
		if (lineCnt == 0)
			return 0;

		int durMS = conn.service.getDuration();
		return ms * lineCnt * itemAtPosition.getHeight() / durMS;

	}

	private int calcScrollY() {
		LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.lll);
		if (ll.getChildCount() <= 0)
			return 0;
		View itemAtPosition = ll.getChildAt(0);

		float pos = conn.service.getCurrentPosition() / 1000f;
		float dur = conn.service.getDuration() / 1000f;

		return (int) (itemAtPosition.getHeight() * lineCnt * pos / dur);
	}

}
