package kvv.aplayer.player;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.os.Handler;

public class MediaPlayer1 {

	private Handler handler = new Handler();

	OnCompletionListener onCompletionListener;
	OnErrorListener onErrorListener;
	OnInfoListener onInfoListener;

	String path;

	private int duration;

	InputStream inputStream;
	Bitstream bitstream;

	Thread1 thread1;

	public void setOnCompletionListener(
			OnCompletionListener onCompletionListener) {
		this.onCompletionListener = onCompletionListener;
	}

	public void setOnErrorListener(OnErrorListener onErrorListener) {
		this.onErrorListener = onErrorListener;
	}

	public void setOnInfoListener(OnInfoListener onInfoListener) {
		this.onInfoListener = onInfoListener;
	}

	public void start() {
		if (thread1 != null)
			thread1.playing = true;
	}

	public void pause() {
		if (thread1 != null)
			thread1.playing = false;
	}

	public void seekTo(int pos) {
		// TODO Auto-generated method stub

	}

	public boolean isPlaying() {
		return thread1 != null && thread1.playing;
	}

	public void stop() {
		try {
			if (thread1 != null)
				thread1.stopped = true;
			thread1 = null;
			if (inputStream != null)
				inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reset() {
		// TODO Auto-generated method stub

	}

	public void setDataSource(String path) {
		this.path = path;
	}

	public void prepare() {
		try {
			stop();
			// ////////////////////////////////
			inputStream = new BufferedInputStream(new FileInputStream(path),
					8 * 1024);
			bitstream = new Bitstream(inputStream);

			long t = System.currentTimeMillis();
			double curPos = 0;
			Decoder decoder = new Decoder();
			System.out.println("playing " + path);
			try {
				for (;;) {
					System.out.print(curPos);
					Header frameHeader = bitstream.readFrame();
					if (frameHeader == null)
						break;
					curPos += frameHeader.ms_per_frame();

					decoder.decodeFrame(frameHeader, bitstream);

					bitstream.closeFrame();

					System.out.println(".");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("time=" + (System.currentTimeMillis() - t));
			System.out.println("len=" + curPos);

			inputStream.close();
			// ////////////////////////////////

			inputStream = new BufferedInputStream(new FileInputStream(path),
					8 * 1024);
			bitstream = new Bitstream(inputStream);

			thread1 = new Thread1();
			thread1.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getCurrentPosition() {
		if (thread1 != null)
			return (int) thread1.curPos;
		return 0;
	}

	public int getDuration() {
		return duration;
	}

	public void release() {
	}

	public void setVolume(int i, int j) {
	}

	class Thread1 extends Thread {
		private AudioTrack audioTrack;
		private volatile boolean stopped;
		private volatile float curPos;
		boolean playing;
		Decoder decoder;

		@Override
		public void run() {
			decoder = new Decoder();

			try {
				while (!stopped) {
					if (!playing) {
						Thread.sleep(100);
						continue;
					}

					Header frameHeader = bitstream.readFrame();
					if (frameHeader == null)
						break;

					curPos += frameHeader.ms_per_frame();

					SampleBuffer output = (SampleBuffer) decoder.decodeFrame(
							frameHeader, bitstream);

					if (audioTrack == null) {
						int sr = output.getSampleFrequency();
						int chan = output.getChannelCount();

						System.out.println("sr = " + sr);
						System.out.println("chan = " + chan);

						if (chan != 2)
							throw new IOException("mono not supported");

						int audioTrackBufSize = AudioTrack.getMinBufferSize(sr,
								AudioFormat.CHANNEL_CONFIGURATION_STEREO,
								AudioFormat.ENCODING_PCM_16BIT);

						audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
								sr, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
								AudioFormat.ENCODING_PCM_16BIT,
								audioTrackBufSize * 4, AudioTrack.MODE_STREAM);
						audioTrack.play();
					}

					short[] pcm = output.getBuffer();
					audioTrack.write(pcm, 0, pcm.length);

					bitstream.closeFrame();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (audioTrack != null) {
				audioTrack.stop();
				audioTrack.release();
			}

			if (!stopped) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (!stopped && thread1 == Thread1.this) {
							MediaPlayer1.this.stop();
							onCompletionListener.onCompletion(null);
						}

					}
				});
			}
		};
	};
}
