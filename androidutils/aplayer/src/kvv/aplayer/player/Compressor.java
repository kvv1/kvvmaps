package kvv.aplayer.player;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;

import com.smartbean.androidutils.util.LPF;
import com.smartbean.androidutils.util.Utils;

public abstract class Compressor {

	private static final int RATE = 16;

	private static final double MEAN = 40;

	private Visualizer visualizer;

	private int db;
	private boolean visible;
	private boolean playing;

	protected abstract void setGain(float db);

	protected abstract void levelChanged(float indicatorLevel);

	interface Alg {
		void setSR(int sr);

		void onSample(int v);

		float calcGain();

		void reset();
	}

	private Alg alg = new Alg1();

	@SuppressLint({ "InlinedApi", "NewApi" })
	public Compressor(MediaPlayer mp) {
		visualizer = new Visualizer(mp.getAudioSessionId());
		visualizer.setScalingMode(Visualizer.SCALING_MODE_AS_PLAYED);
		visualizer.setDataCaptureListener(new OnDataCaptureListener2() {
		}, RATE * 1000, true, false);
	}

	public void release() {
		visualizer.release();
	}

	public void setComprLevel(int db) {
		this.db = db;
		enDis();
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
		enDis();
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		enDis();
	}

	private void enDis() {
		boolean b = playing && (visible | db != 0);
		System.out.println("setEnabled " + b);
		visualizer.setEnabled(b);
		if (!b) {
			if (levelLPF != null)
				levelLPF.set(0);
			levelChanged(0);
			setGain(0);
		}
	}

	public void resetGain() {
		alg.reset();
	}

	public int getComprLevel() {
		return db;
	}


	private int sr;

	private LPF levelLPF;

	class OnDataCaptureListener2 implements OnDataCaptureListener {

		@Override
		public void onWaveFormDataCapture(Visualizer visualizer,
				byte[] waveform, int samplingRate) {

			if (samplingRate != sr) {
				System.out.println("sr=" + samplingRate);
				alg.setSR(samplingRate / 1000);
				levelLPF = new LPF(samplingRate / 1000, 0.02, 0.2);
				sr = samplingRate;
			}

			for (byte b : waveform) {
				int a = ((int) b & 0xFF) - 128;
				alg.onSample(a);
				int a1 = Math.max(Math.abs(a) - 1, 0);
				levelLPF.add(a1);
			}

			levelChanged((float) (levelLPF.get() / (MEAN)));

			float gain = alg.calcGain();

			if (db != 0)
				setGain(gain);
			else
				setGain(0);
		}

		@Override
		public void onFftDataCapture(Visualizer arg0, byte[] arg1, int arg2) {
		}
	}

	private float bounds(float min, float val, float max) {
		return Math.max(min, Math.min(val, max));
	}

	class Alg1 implements Alg {
		private LPF lpf;

		@Override
		public void setSR(int sr) {
			lpf = new LPF(sr, 0.01, 5);
			// lpf = new LPF(sr, 0.01, 1);
		}

		@Override
		public void onSample(int a) {
			int a1 = Math.abs(a);
			lpf.add(a1);
		}

		@Override
		public float calcGain() {
			double mean = lpf.get();

			double k = MEAN / mean;
			double maxK = Utils.db2n(db);

			if (k > maxK)
				k = maxK;

			float gain = (float) Utils.n2db(k);

			//System.out.printf("m=%f g=%f\n", mean, gain);

			gain = bounds(-db, gain, db);
			return gain;
		}

		@Override
		public void reset() {
			if (lpf != null)
				lpf.set(MEAN / 2);
		}

	}

	class Alg2 implements Alg {
		private static final double MEAN = 20;

		private LPF lpf;
		private float gain;

		@Override
		public void setSR(int sr) {
			lpf = new LPF(sr, 0.01, 1);
		}

		@Override
		public void onSample(int a) {
			int a1 = Math.abs(a);
			lpf.add(a1);
		}

		@Override
		public float calcGain() {
			double mean = lpf.get();

			if (mean < MEAN)
				gain += 0.1;
			else {
				double k = MEAN / mean;
				float g = (float) Utils.n2db(k);
				gain += g;
				lpf.set(MEAN);
			}
			System.out.printf("m=%f g=%f\n", mean, gain);
			return gain;
		}

		@Override
		public void reset() {
			if (lpf != null)
				lpf.set(MEAN / 3);
			gain = 5;
		}

	}

}
