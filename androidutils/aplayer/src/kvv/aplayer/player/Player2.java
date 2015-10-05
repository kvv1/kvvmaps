package kvv.aplayer.player;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import kvv.aplayer.service.File1;
import kvv.aplayer.service.Folder;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class Player2 implements IPlayer {
	private static final int SAMPLE_RATE = 44100;

	private int curFolder;
	private int curFile;
	private int curPos;
	private List<Folder> folders;

	public Player2(List<Folder> folders) {
		this.folders = folders;
	}

	public void onChanged(OnChangedHint hint) {

	}

	public static byte[] decode(String path, int startMs, int maxMs)
			throws IOException, DecoderException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);

		float totalMs = 0;
		boolean seeking = true;

		File file = new File(path);
		InputStream inputStream = new BufferedInputStream(new FileInputStream(
				file), 8 * 1024);
		try {
			Bitstream bitstream = new Bitstream(inputStream);
			Decoder decoder = new Decoder();

			boolean done = false;
			while (!done) {
				Header frameHeader = bitstream.readFrame();
				if (frameHeader == null) {
					done = true;
				} else {
					totalMs += frameHeader.ms_per_frame();

					if (totalMs >= startMs) {
						seeking = false;
					}

					if (!seeking) {
						SampleBuffer output = (SampleBuffer) decoder
								.decodeFrame(frameHeader, bitstream);

						if (output.getSampleFrequency() != 44100
								|| output.getChannelCount() != 2) {
							throw new IOException(
									"mono or non-44100 MP3 not supported");
						}

						short[] pcm = output.getBuffer();
						for (short s : pcm) {
							outStream.write(s & 0xff);
							outStream.write((s >> 8) & 0xff);
						}
					}

					if (totalMs >= (startMs + maxMs)) {
						done = true;
					}
				}
				bitstream.closeFrame();
			}

			return outStream.toByteArray();
		} catch (BitstreamException e) {
			throw new IOException("Bitstream error: " + e);
		} catch (DecoderException e) {
			Log.w("DECODER", "Decoder error", e);
			throw e;
		} finally {
			inputStream.close();
		}
	}

	class Thread1 extends Thread {
		private AudioTrack audioTrack;
		public String path;
		private volatile boolean stopped;

		@Override
		public void run() {

			File file = new File(path);

			float totalMs = 0;

			InputStream inputStream = null;

			try {
				inputStream = new BufferedInputStream(
						new FileInputStream(file), 8 * 1024);

				Bitstream bitstream = new Bitstream(inputStream);
				Decoder decoder = new Decoder();

				while (!stopped) {
					Header frameHeader = bitstream.readFrame();
					if (frameHeader == null)
						break;

					totalMs += frameHeader.ms_per_frame();

					SampleBuffer output = (SampleBuffer) decoder.decodeFrame(
							frameHeader, bitstream);

					int sr = output.getSampleFrequency();
					int chan = output.getChannelCount();

					if (audioTrack == null) {
						System.out.println("sr = " + sr);
						System.out.println("chan = " + chan);

						if (chan != 2)
							throw new IOException(
									"mono not supported");

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

			} catch (BitstreamException e) {
				e.printStackTrace();
			} catch (DecoderException e) {
				Log.w("DECODER", "Decoder error", e);
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (inputStream != null)
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}

			audioTrack.stop();
			audioTrack.release();
			thread1 = null;
		};
	};

	volatile Thread1 thread1;

	@Override
	public void play() {
		if (thread1 != null) {
			thread1.stopped = true;
		}

		thread1 = new Thread1();

		File1 f = folders.get(curFolder).files[curFile];
		thread1.path = f.path;

		System.out.println("playing " + f.path);

		thread1.start();
	}

	@Override
	public void pause() {
		if (thread1 != null) {
			thread1.stopped = true;
			thread1 = null;
		}
	}


	@Override
	public void prev() {
		// TODO Auto-generated method stub

	}

	@Override
	public void next() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPlaying() {
		return thread1 != null;
	}

	@Override
	public void setGain(int int1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCompr(int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDbPer100(float f) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getCurrentFolder() {
		// TODO Auto-generated method stub
		return curFolder;
	}

	@Override
	public List<Folder> getFolders() {
		return folders;
	}

	@Override
	public void toFolder(int folder, int file, int pos) {
		curFolder = folder;
		curFile = file;
		curPos = pos;
		play();
		onChanged(OnChangedHint.FOLDER);
	}

	@Override
	public void makeRandom(int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public void toFile(int file) {
		curFile = file;
		play();
		onChanged(OnChangedHint.FOLDER);
	}

	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		return 100;
	}

	@Override
	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void seek(int seekStep) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getFile() {
		return curFile;
	}

	@Override
	public int getGain() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getIndicatorLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setVisible(boolean vis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSpeedKMH(float speed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
