package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.service.Folder;
import android.media.audiofx.Equalizer;
import android.widget.Chronometer.OnChronometerTickListener;

public abstract class Player1 extends Player {

	private final Compressor compr;
	private Equalizer eq;

	private short[] bandRange;
	private short nBands;

	private int gain;
	private float comprGain;
	private float dBPer100;
	private float speedKMH;
	private boolean visible;

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

		setGain(0);
	}

	@Override
	protected void onChanged(OnChangedHint hint) {
		if (compr != null)
			compr.enDis(visible);
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
		compr.enDis(visible);
		setEq();
	}

	public void setDbPer100(float dBPer100) {
		this.dBPer100 = dBPer100;
		setEq();
	}

	public void setSpeedKMH(float speed) {
		this.speedKMH = speed;
		System.out.println("speed=" + speed);
		setEq();
	}

	private void setEq() {
		float g = gain;
		g += speedKMH * dBPer100 / 100;
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

	public void setVisible(boolean vis) {
		visible = vis;
		compr.enDis(visible);
		// TODO Auto-generated method stub

	}
}
