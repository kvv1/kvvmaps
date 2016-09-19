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
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartbean.androidutils.util.AsyncCallback;

public class TextSectionFragment extends FilesSectionFragmentBase {

	private static final int STEP = 1000;

	private int lineCnt;
	private String lastTextPath;
	private Handler handler = new Handler();

	private Translator translator;

	private Runnable r = new Runnable() {
		@Override
		public void run() {
			ScrollView sv = (ScrollView) rootView.findViewById(R.id.scroll);
			if (conn.service != null && conn.service.isPlaying() && sv != null) {
				int scrollY = sv.getScrollY();
				sv.scrollTo(0, scrollY + (int) dScroll(STEP));
			}
			handler.postDelayed(this, STEP);
		}
	};

	private PlayerListener listener = new PlayerAdapter() {

		public void fileChanged() {
			final List<String> lines = new ArrayList<String>();

			Files files = conn.service.getFiles();
			if (files != null && files.curFile >= 0) {
				FileDescriptor fileDescriptor = files.files.get(files.curFile);
				int idx = fileDescriptor.path.lastIndexOf('.');
				String textPath = fileDescriptor.path.substring(0, idx)
						+ ".txt";

				if (textPath.equals(lastTextPath))
					return;

				lastTextPath = textPath;

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
			}

			lineCnt = lines.size();

			for (int i = 0; i < 5; i++)
				lines.add(0, "");
			for (int i = 0; i < 5; i++)
				lines.add("");

			LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.lll);
			ScrollView sv = (ScrollView) rootView.findViewById(R.id.scroll);
			sv.scrollTo(0, 0);
			ll.removeAllViews();
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

								// AlertDialog.Builder builder = new
								// AlertDialog.Builder(
								// getActivity());
								// builder.setMessage(str
								// + "\n"
								// + res
								// + "\n"
								// + "Переведено сервисом «Яндекс.Переводчик»");
								// builder.create().show();

								// Toast.makeText(getActivity(), res,
								// Toast.LENGTH_LONG).show();
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
		System.out.println("TextSectionFragment.createUI");

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

	@Override
	public void onPause() {
		handler.removeCallbacksAndMessages(null);
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		handler.post(r);
	}

	@Override
	protected boolean isLevelNeeded() {
		return false;
	}

	@Override
	protected void folderProgressClicked() {
	}

}
