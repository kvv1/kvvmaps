package kvv.aplayer;

import java.util.List;

import android.media.audiofx.Equalizer;

public abstract class Player1 extends Player {

	private final Compressor compr;
	private Equalizer eq;

	private short[] bandRange;
	private short nBands;

	private int gain;
	private float dBPer100;
	private float speed;

	public Player1(List<Folder> folders) {
		super(folders);
		compr = new Compressor(mp);
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

	public void setCompr(boolean b) {
		compr.setAuto(b);
	}

	public boolean getCompr() {
		return compr.getAuto();
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
		float k = compr.getK();
		int comprDB = (int) (20 * Math.log10(1 / k));

		float g = gain;
		g += speed * dBPer100 / 100;

		System.out.println("g=" + g);

		setEq(comprDB + (int) (g * 100));
	}

	private void setEq(int level) {
		if (eq.getEnabled())
			for (short i = 0; i < nBands; i++) {
				eq.setBandLevel(i, (short) level);
			}
	}

	@Override
	protected void resetGain() {
		compr.resetGain();
	}
}
