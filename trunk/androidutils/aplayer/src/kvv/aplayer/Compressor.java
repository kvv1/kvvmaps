package kvv.aplayer;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;

public class Compressor {

	private MediaPlayer mp;

	private Visualizer visualizer;

	private volatile boolean autoVol = false;

	private volatile float k = 0.5f;
	private volatile float gain;

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

	public void setAuto(boolean b) {
		autoVol = b;
	}

	public void setEnabled(boolean b) {
		visualizer.setEnabled(b);
	}

	private void setVol(float v) {
		if (mp != null)
			mp.setVolume(v, v);
	}

	public void resetGain() {
		gain = k;
	}

	public void setK(float k) {
		this.k = k;
		resetGain();
	}

	private LPF lpf;
	private LPF lpf2;
	private int sr;

	class OnDataCaptureListener2 implements OnDataCaptureListener {

		@Override
		public void onWaveFormDataCapture(Visualizer arg0, byte[] waveform,
				int samplingRate) {

			if (samplingRate != sr) {
				lpf = new LPF(samplingRate / 1000, 2, 2);
				lpf2 = new LPF(samplingRate / 1000, 2, 2);
				sr = samplingRate;
			}

			int max = 0;

			for (byte b : waveform) {
				int a = ((int) b & 0xFF) - 128;
				a = Math.abs(a);
				max = Math.max(max, a);
				lpf.add(a);
				lpf2.add(a * a);
			}

			if (max == 0)
				max = 1;

			float v = max / 128f;

			float v1 = k / v;
			if (v1 > 1)
				v1 = 1;

			if (v1 > gain)
				gain += 0.02f * gain;
			else if (v1 < gain)
				gain -= 0.05f * gain;

			if (gain > 1)
				gain = 1;
			if (gain < k)
				gain = k;

//			System.out.println((int) lpf.get() + " " + (int) lpf2.get() + " "
//					+ max + " " + v1 + " " + gain);

			if (autoVol) {
				setVol(gain);
			} else {
				setVol(k);
			}
		}

		@Override
		public void onFftDataCapture(Visualizer arg0, byte[] arg1, int arg2) {
		}
	}

	public float getK() {
		return k;
	}

	public boolean getAuto() {
		return autoVol;
	}
}
