package kvv.manchester;

import kvv.manchester.ManchesterEncoder;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Transmitter {
	private volatile boolean stop;

	private byte[] data;
	private final byte[] emptyData;

	public Transmitter() {
//		emptyData = ManchesterEncoder.encodeRaw(new byte[32]);
		emptyData = new byte[ManchesterEncoder.encodeRaw(new byte[32]).length];
	}

	public synchronized void send(byte[] data) {
		while (this.data != null)
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		this.data = data;
	}

	private Thread thread = new Thread() {
		@Override
		public void run() {
			int bufSize = AudioTrack.getMinBufferSize(44100,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_8BIT);
			System.out.println("AudioTrack.getMinBufferSize = " + bufSize);

			final AudioTrack audioTrack = new AudioTrack(
					AudioManager.STREAM_MUSIC, 44100,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_8BIT, bufSize,
					AudioTrack.MODE_STREAM);

			audioTrack.play();

			int pos = 0;
			while (!stop) {
				byte[] d = null;
				synchronized (Transmitter.this) {
					if (data != null) {
						d = data;
						data = null;
						Transmitter.this.notify();
					}
				}

				if (d == null)
					d = emptyData;

				audioTrack.write(d, pos, d.length);
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
