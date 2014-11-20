package kvv.aplayer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class MyAudioTrack{
	private static final int SAMPLE_RATE = 22050;

	int bufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
			AudioFormat.CHANNEL_CONFIGURATION_MONO,
			AudioFormat.ENCODING_PCM_16BIT);

	final AudioTrack audioTrack = new AudioTrack(
			AudioManager.STREAM_MUSIC, SAMPLE_RATE,
			AudioFormat.CHANNEL_CONFIGURATION_MONO,
			AudioFormat.ENCODING_PCM_16BIT, bufSize,
			AudioTrack.MODE_STREAM);
	
	private volatile boolean stop;
	
	
	public MyAudioTrack()
			throws IllegalArgumentException {
		audioTrack.play();
	}

	private Thread thread = new Thread() {
		@Override
		public void run() {

			audioTrack.play();

			short[] buf = new short[4096];
			
			while (!stop) {
				for(int i = 0; i < buf.length; i++) {
					buf[i] = (short) (3000 * Math.sin(2 * Math.PI * i * 200 / 4096));
				}
				audioTrack.write(buf, 0, buf.length);
			}

			audioTrack.stop();
			audioTrack.release();
		};
	};

	public void start() {
		thread.start();
	}

	public void stop() {
		stop = true;
	}
}
