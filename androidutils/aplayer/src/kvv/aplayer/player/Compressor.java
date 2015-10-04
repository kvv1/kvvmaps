package kvv.aplayer.player;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;

import com.smartbean.androidutils.util.Utils;

public abstract class Compressor {

	private static final int RATE = 16;

	//private static final double MEAN = 20;
	private static final double MEAN = 200;

	private MediaPlayer mp;

	private Visualizer visualizer;

	private int db;

	protected abstract void setGain(float db);

	protected abstract void onLevel(float v);

	public Compressor(MediaPlayer mp) {
		this.mp = mp;
	}

	@SuppressLint("NewApi")
	public void init() {
		visualizer = new Visualizer(mp.getAudioSessionId());
		visualizer.setScalingMode(Visualizer.SCALING_MODE_AS_PLAYED);
		visualizer.setDataCaptureListener(new OnDataCaptureListener2() {
		}, RATE * 1000, true, false);
	}

	public void release() {
		mp = null;
		visualizer.release();
	}

	public void setComprLevel(int db) {
		this.db = db;
	}

	public int getComprLevel() {
		return db;
	}

	byte[] wave;

	public void setEnabled(boolean b) {
		System.out.println("setEnabled " + b);
		visualizer.setEnabled(b);
		if (!b) {
			if (levelLPF != null)
				levelLPF.set(0);
			onLevel(0);
			setGain(0);
		}

	}

	public void enDis(boolean visible) {
		setEnabled(mp.isPlaying() && (visible | db != 0));
	}

	private LPF lpf;
	private int sr;

	private LPF levelLPF;

	class OnDataCaptureListener2 implements OnDataCaptureListener {

		@Override
		public void onWaveFormDataCapture(Visualizer visualizer,
				byte[] waveform, int samplingRate) {

			wave = waveform;

			if (samplingRate != sr) {
				System.out.println("sr=" + samplingRate);
				lpf = new LPF(samplingRate / 1000, 0.1, 5);
//				lpf = new LPF(samplingRate / 1000, 0.1, 0.1);
				levelLPF = new LPF(samplingRate / 1000, 0.02, 0.5);
				sr = samplingRate;
			}

			int max = 0;

			for (byte b : waveform) {
				int a = ((int) b & 0xFF) - 128;
				int a1 = Math.abs(a);
				a1 *= 3;
				max = Math.max(max, a1);

				lpf.add(a1);
				levelLPF.add(a1);
			}

			if (visualizer.getEnabled())
				onLevel((float) (levelLPF.get() / MEAN));

			//float gain = calcGain
			
			double mean = lpf.get();

			float level = (float) Utils.n2db(mean / MEAN);
			float gain = (float) Utils.n2db(MEAN / mean);
			
			if (gain > db)
				gain = db;
			if (gain < -db)
				gain = -db;

			// setGain(gain);

			System.out.printf("m=%f g=%f\n", mean, gain);

			if (db != 0 && visualizer.getEnabled()) {
				setGain(gain);
			} else {
				setGain(0);
			}
		}

		@Override
		public void onFftDataCapture(Visualizer arg0, byte[] arg1, int arg2) {
		}
	}

	public void resetGain() {
		if (lpf != null)
			lpf.set(MEAN / 3);
	}

	public void test() {
	}

	public void setSource(String path) {
	}

}
