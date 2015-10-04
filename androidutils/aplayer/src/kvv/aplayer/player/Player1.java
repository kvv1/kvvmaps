package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.service.Folder;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;

import com.smartbean.androidutils.util.Utils;

public abstract class Player1 extends Player implements IPlayer {

	private final Compressor compr;

	private final Eq eq;

	// private final Eq1 eq1;

	private int gain;
	private float comprGain;
	private float dBPer100;
	private float speedKMH;
	private boolean visible;

	private volatile float level;

	public Player1(List<Folder> folders) {
		super(folders);

		// eq = new Eq(mp);
		eq = new EqEq(mp, -15);

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

		setEq(0);

		compr.init();

		setGain(0);
	}

	@Override
	public void onChanged(OnChangedHint hint) {
		if (compr != null) {
			compr.enDis(visible);
			if ((hint == OnChangedHint.FILE || hint == OnChangedHint.FOLDER)
					&& getFolders().get(getCurrentFolder()).files.length > 0)
				compr.setSource(getFolders().get(getCurrentFolder()).files[getFile()].path);
		}
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
		compr.test();
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
		// g += speedKMH * dBPer100 / 100;
		g += comprGain;
		setEq(g);

		// eq1.setGain(speedKMH * dBPer100 / 100);
	}

	private void setEq(float level) {
		System.out.println("setEq " + level);
		eq.setGain(level);
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
	}
}

interface Eq {
	void setGain(float gain);

	void release();
}

class EqEq implements Eq {
	private final Equalizer equalizer;
	private short[] bandRange;
	private short nBands;
	private final float refDb;

	public EqEq(MediaPlayer mp, float refDb) {
		equalizer = new Equalizer(10, mp.getAudioSessionId());
		nBands = equalizer.getNumberOfBands();
		bandRange = equalizer.getBandLevelRange();
		this.refDb = refDb;

		System.out.println("bands=" + nBands);
		System.out.println("min=" + bandRange[0] + " max=" + bandRange[1]);
		equalizer.setEnabled(true);
	}

	@Override
	public void setGain(float gain) {
		gain += refDb;
		if (equalizer.getEnabled())
			for (short i = 0; i < nBands; i++)
				equalizer.setBandLevel(i, (short) (gain * 100));
	}

	@Override
	public void release() {
		equalizer.release();
	}
}

class EqVol implements Eq {
	private final MediaPlayer mp;
	private final float refDb;

	public EqVol(MediaPlayer mp, float refDb) {
		this.mp = mp;
		this.refDb = refDb;
	}

	@Override
	public void setGain(float gain) {
		float n = (float) Utils.db2n(gain + refDb);
		mp.setVolume(n, n);
	}

	@Override
	public void release() {
	}
}
