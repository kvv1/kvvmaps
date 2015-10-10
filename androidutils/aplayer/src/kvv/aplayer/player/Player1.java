package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.service.Folder;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;

import com.smartbean.androidutils.util.Utils;

public abstract class Player1 extends Player0 {

	private final Compressor compr;

	private Eq eqCompr;

	private Eq eqSpeed;

	private int gain;
	private float comprGain;
	private float dBPer100;
	private float speedKMH;

	@Override
	public void onChanged(OnChangedHint hint) {
		if (compr != null)
			compr.enDis();
	}

	public Player1(List<Folder> folders) {
		super(folders);

		eqCompr = new EqVol(getMP(), 0);
		eqSpeed = new EqEq(getMP(), 0);

		compr = new Compressor(getMP()) {
			@Override
			protected void setGain(float db) {
				comprGain = db;
				setEq();
			}

			@Override
			protected void levelChanged(float indicatorLevel) {
				Player1.this.levelChanged(indicatorLevel);
			}
		};

		setEq();

		compr.init();

		setGain(0);
	}

	protected abstract void levelChanged(float indicatorLevel);

	@Override
	public void close() {
		eqCompr.release();
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
		compr.test();
		if (eqCompr != null)
			eqCompr.release();
		eqCompr = new EqVol(getMP(), -db);
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

		System.out.println("setEq " + g);
		eqCompr.setGain(g);

		if (eqSpeed == null)
			eqSpeed = new EqEq(getMP(), 0);
		
		eqSpeed.setGain(speedKMH * dBPer100 / 100 - dBPer100 * 1.2f);
	}

	@Override
	protected void resetGain() {
		compr.resetGain();
	}

	public void setVisible(boolean vis) {
		compr.setVisible(vis);
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
