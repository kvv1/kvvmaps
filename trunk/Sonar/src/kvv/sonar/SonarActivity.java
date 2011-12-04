package kvv.sonar;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.view.KeyEvent;

public class SonarActivity extends Activity {

	private SonarView view;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		view = (SonarView) findViewById(R.id.view1);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

			int[] data = new int[100];

			data[10] = 100;
			data[20] = -50;
			data[90] = 75;

			view.setData(data);

			byte[] audioData = new byte[44100];

			double freq = 10000;
			int msec = 1;
			int startPoint = 20000;
			
			int samples = msec * 44100 / 1000;
			

			int k = 100;
			for (int i = 0; i < audioData.length; i++) {
				int v = (int) (128 + 127 * Math.sin(2 * Math.PI * k++
						* freq / audioData.length));
				
				if(i < startPoint)
					v = 0;
				if(i >= startPoint && i < startPoint + samples / 10)
					v = v * (i - startPoint) / (samples / 10);
				if(i > startPoint + samples - samples / 10 && i < startPoint + samples)
					v = v * (startPoint + samples - i) / (samples / 10);
				if(i >= startPoint + samples)
					v = 0;
				
				audioData[i] = (byte) v;
			}

			long t = System.currentTimeMillis();
			
			System.out.println("1 " + (System.currentTimeMillis() - t));
			
			final AudioTrack audioTrack = new AudioTrack(
					AudioManager.STREAM_MUSIC, 44100,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_8BIT, audioData.length,
					AudioTrack.MODE_STREAM);

			System.out.println("2 " + (System.currentTimeMillis() - t));
			
			audioTrack.write(audioData, 0, audioData.length);
			
			System.out.println("3 " + (System.currentTimeMillis() - t));
			audioTrack.play();
			System.out.println("4 " + (System.currentTimeMillis() - t));

			int bufferSize = AudioRecord.getMinBufferSize(44100,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			
			System.out.println("5 " + (System.currentTimeMillis() - t));
			bufferSize = 44100 * 2;
			
			AudioRecord audioRecord = new AudioRecord(AudioSource.MIC, 44100,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bufferSize * 4);

			System.out.println("6 " + (System.currentTimeMillis() - t));
			short[] inBuffer = new short[44100];
			
			audioRecord.startRecording();
			System.out.println("7 " + (System.currentTimeMillis() - t));
			int n = audioRecord.read(inBuffer, 0, inBuffer.length);

			System.out.println("8 " + (System.currentTimeMillis() - t));
			
			
			audioTrack.stop();
			audioTrack.release();
			
			audioRecord.stop();
			
			audioRecord.release();

			data = new int[inBuffer.length];
			for(int i = 0; i < inBuffer.length; i++)
				data[i] = inBuffer[i];
			view.setData(data);

			return true;
		}
		return false;
	}
}