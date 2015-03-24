package kvv.aplayer;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;

public abstract class Compressor {

	private static final double MEAN = 90;


	private MediaPlayer mp;

	private Visualizer visualizer;

	private volatile boolean autoVol = false;
	private volatile int db;
	private volatile float gain;

	protected abstract void setGain(float db);

	public Compressor(MediaPlayer mp) {
		this.mp = mp;
	}

	public void init() {
		visualizer = new Visualizer(mp.getAudioSessionId());

		visualizer.setDataCaptureListener(new OnDataCaptureListener2() {
		}, 8000, true, false);

		visualizer.setEnabled(true);
	}

	public void release() {
		mp = null;
		visualizer.release();
	}

	public void setComprLevel(int db) {
		autoVol = db != 0;
		this.db = db;
	}

	public void setEnabled(boolean b) {
		visualizer.setEnabled(b);
	}

	private LPF lpf;
	private int sr;

	class OnDataCaptureListener2 implements OnDataCaptureListener {

		@Override
		public void onWaveFormDataCapture(Visualizer arg0, byte[] waveform,
				int samplingRate) {

			if (samplingRate != sr) {
				lpf = new LPF(samplingRate / 1000, 0.01, 0.5);
				sr = samplingRate;
			}

			int max = 0;

			for (byte b : waveform) {
				int a = ((int) b & 0xFF) - 128;
				a = Math.abs(a);
				// a *= gain;
				max = Math.max(max, a);
				lpf.add(a);
			}

			double mean = lpf.get();

			gain = (float) n2db(MEAN / mean);
			if (gain > db)
				gain = db;

			setGain(gain);

			System.out.printf("m=%f g=%f\n", mean, gain);

			if (autoVol) {
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
		if(lpf != null)
			lpf.set(MEAN);
	}
}
