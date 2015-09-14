package kvv.aplayer.player;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;

public abstract class Compressor {

	private static final double MEAN = 90;

	private MediaPlayer mp;

	private Visualizer visualizer;

	private volatile int db;

	protected abstract void setGain(float db);

	protected abstract void onLevel(float v);

	public Compressor(MediaPlayer mp) {
		this.mp = mp;
	}

	public void init() {
		visualizer = new Visualizer(mp.getAudioSessionId());
		visualizer.setDataCaptureListener(new OnDataCaptureListener2() {
		}, 16000, true, false);
		visualizer.setEnabled(true);
	}

	public void release() {
		mp = null;
		visualizer.release();
	}

	public void setComprLevel(int db) {
		this.db = db;
	}

	private volatile boolean en;

	public void setEnabled(boolean b) {
		visualizer.setEnabled(b);
		en = b;
		if (!b) {
			if (lpfLevel != null)
				lpfLevel.set(0);
			onLevel(0);
		}
	}

	private LPF lpf;
	private int sr;

	private LPF lpfLevel;

	class OnDataCaptureListener2 implements OnDataCaptureListener {

		@Override
		public void onWaveFormDataCapture(Visualizer arg0, byte[] waveform,
				int samplingRate) {

			if (samplingRate != sr) {
				System.out.println("sr=" + samplingRate);
				lpf = new LPF(samplingRate / 1000, 0.01, 5);
				lpfLevel = new LPF(samplingRate / 1000, 0.1, 0.1);
				sr = samplingRate;
			}

			int max = 0;

			for (byte b : waveform) {
				int a = ((int) b & 0xFF) - 128;
				int a1 = Math.abs(a);
				// a *= gain;
				max = Math.max(max, a1);
				lpf.add(a1);
				if (en)
					lpfLevel.add(a1);

			}

			onLevel((float) lpfLevel.get());

			double mean = lpf.get();

			float gain = (float) n2db(MEAN / mean);
			if (gain > db)
				gain = db;

			// setGain(gain);

			//System.out.printf("m=%f g=%f\n", mean, gain);

			if (db != 0) {
				setGain(gain);
			} else {
				setGain(0);
			}
		}

		@Override
		public void onFftDataCapture(Visualizer arg0, byte[] arg1, int arg2) {
		}
	}

	public static double n2db(double n) {
		return 20 * Math.log10(n);
	}

	public static double db2n(double db) {
		return Math.pow(10, db / 20);
	}

	public void resetGain() {
		if (lpf != null)
			lpf.set(MEAN);
	}
}
