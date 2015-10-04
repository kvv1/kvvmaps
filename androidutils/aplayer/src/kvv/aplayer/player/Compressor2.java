package kvv.aplayer.player;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;

public abstract class Compressor2 {

	private final MediaPlayer mp;
	private int db;

	public Compressor2(MediaPlayer mp) {
		this.mp = mp;
	}

	protected abstract void setGain(float db);

	protected abstract void onLevel(float v);

	public void setComprLevel(int db) {
		this.db = db;
	}

	public void enDis(boolean visible) {
		setEnabled(mp.isPlaying() && (visible | db != 0));
	}

	private final static int STEP_MS = 100;

	private int[] levels = new int[3600 * 1000 / STEP_MS];

	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			int idx = mp.getCurrentPosition() / STEP_MS;
			if (idx < levels.length) {
				int l;
				synchronized (levels) {
					l = levels[idx];
				}
				if (l > 0)
					onLevel(l * 128 / 10000);
			}

			handler.postDelayed(this, 100);
		}
	};

	public void setEnabled(boolean b) {
		handler.removeCallbacksAndMessages(null);
		if (b)
			handler.post(runnable);

		if (!b) {
			onLevel(0);
			setGain(0);
		}
	}

	public void init() {
	}

	public void release() {
		handler.removeCallbacksAndMessages(null);
	}

	public void test() {
	}

	public void resetGain() {
	}

	public void setSource(final String path) {
		System.out.println("setSource " + path);
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				long time = System.currentTimeMillis();
				
				synchronized (levels) {
					Arrays.fill(levels, -1);
				}

				InputStream inputStream = null;
				try {
					inputStream = new BufferedInputStream(new FileInputStream(
							path), 8 * 1024);
					Bitstream bitstream = new Bitstream(inputStream);
					Decoder decoder = new Decoder();

					LPF lpfLevel = null;
					double curPos = 0;
					int idx = 0;

					for (;;) {
						Header frameHeader = bitstream.readFrame();
						if (frameHeader == null)
							break;

						curPos += frameHeader.ms_per_frame();

						SampleBuffer output = (SampleBuffer) decoder
								.decodeFrame(frameHeader, bitstream);

						if (lpfLevel == null) {
							int sr = output.getSampleFrequency();
							int chan = output.getChannelCount();

							System.out.println("sr = " + sr);
							System.out.println("chan = " + chan);

							lpfLevel = new LPF(sr, 0.1, 0.1);
						}

						int idx1 = ((int) curPos / STEP_MS);

						if (idx1 != idx && idx < levels.length) {
							synchronized (levels) {
								levels[idx] = (int) lpfLevel.get();
							}
							idx = idx1;
							
							//System.out.println(lpfLevel.get());
						}

						short[] pcm = output.getBuffer();

						for (short s : pcm)
							lpfLevel.add(Math.abs(s));

						bitstream.closeFrame();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (inputStream != null)
						try {
							inputStream.close();
						} catch (IOException e) {
						}
				}

				System.out.println("length = " + (System.currentTimeMillis() - time));
				
				return null;
			}
		}.execute(new Void[] {});
	}
}
