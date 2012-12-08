package kvv.manchester;

import kvv.manchester.ManchesterDecoder;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

public class Receiver {
	private volatile boolean stop;

	private final ManchesterDecoder decoder;

	public Receiver(ManchesterDecoder decoder) {
		this.decoder = decoder;
	}

	private Thread thread = new Thread() {
		@Override
		public void run() {
			int bufferSize = AudioRecord.getMinBufferSize(44100,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT);

			AudioRecord audioRecord = new AudioRecord(AudioSource.MIC, 44100,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bufferSize * 4);

			audioRecord.startRecording();

			short[] inBuffer = new short[bufferSize];

			boolean prev = false;
			int cnt = 0;
			while (!stop) {
				int n = audioRecord.read(inBuffer, 0, inBuffer.length);
				for (int i = 0; i < n; i++) {
					short s = inBuffer[i];
					boolean b = s > 0;
					cnt++;
					if (b != prev) {
						decoder.transitionReceived(cnt);
						cnt = 0;
					}
					prev = b;
				}
			}

			audioRecord.stop();
			audioRecord.release();
		};
	};

	public void start() {
		thread.start();
	}

	public void stop() {
		stop = true;
	}

}
