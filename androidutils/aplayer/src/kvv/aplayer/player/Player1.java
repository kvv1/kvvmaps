package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.folders.Folder;
import android.media.audiofx.Equalizer;

public abstract class Player1 extends Player {

	private final Compressor compr;
	private Equalizer eq;

	private short[] bandRange;
	private short nBands;

	private volatile int gain;
	private volatile float comprGain;
	private volatile float dBPer100;
	private volatile float speed;

	private volatile float level;

	public Player1(List<Folder> folders) {
		super(folders);
		compr = new Compressor(mp) {
			@Override
			protected void setGain(float db) {
				comprGain = db;
				setEq();
			}

			@Override
			protected void onLevel(float v) {
				level = v;
			}
		};
		eq = new Equalizer(0, mp.getAudioSessionId());

		nBands = eq.getNumberOfBands();
		bandRange = eq.getBandLevelRange();

		System.out.println("min=" + bandRange[0] + " max=" + bandRange[1]);
		eq.setEnabled(true);
		setEq(0);

		compr.init();

		eq.setEnabled(true);
		compr.setEnabled(true);

		setGain(0);
	}

	@Override
	public void close() {
		eq.release();
		compr.release();
		super.close();
	}

	public void setGain(int db) {
		gain = db;
		setEq();
	}

	public int getGain() {
		return gain;
	}

	public void setCompr(int db) {
		compr.setComprLevel(db);
		setEq();
	}

	public void enVis() {
		boolean b = mp.isPlaying();
		System.out.println("*** " + b);
		compr.setEnabled(b);
	}

	public void setDbPer100(float dBPer100) {
		this.dBPer100 = dBPer100;
		setEq();
	}

	public void setSpeedKMH(float speed) {
		this.speed = speed;
		System.out.println("speed=" + speed);
		setEq();
	}

	private void setEq() {
		float g = gain;
		g += speed * dBPer100 / 100;

		g += comprGain;

		setEq(g * 100);
	}

	private void setEq(float level) {
		if (eq.getEnabled())
			for (short i = 0; i < nBands; i++)
				eq.setBandLevel(i, (short) level);
	}

	@Override
	protected void resetGain() {
		compr.resetGain();
	}

	public float getLevel() {
		return level;
	}
}
