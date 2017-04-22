package kvv.aplayer.player;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import kvv.aplayer.service.Folder;

public abstract class Player1 extends Player0 {

	public interface PlayerLevelListener {
		void levelChanged(float indicatorLevel);
	}

	public Collection<PlayerLevelListener> levelListeners = new HashSet<PlayerLevelListener>();

	public void addLevelListener(PlayerLevelListener listener) {
		levelListeners.add(listener);
		compr.setVisible(!levelListeners.isEmpty());
	}

	public void removeLevelListener(PlayerLevelListener listener) {
		levelListeners.remove(listener);
		compr.setVisible(!levelListeners.isEmpty());
	}

	private final Compressor compr;

	private float comprGain;
	private float dBPer100;
	private float speedKMH;

	public Player1(Context context, List<Folder> folders) {
		super(context, folders);

		addListener(new PlayerAdapter() {
			@Override
			public void fileChanged() {
				if (compr != null)
					compr.setPlaying(isPlaying());
			}
		});

		compr = new Compressor(getMP()) {
			@Override
			protected void setGain(float db) {
				comprGain = db;
				setEq();
			}

			@Override
			protected void levelChanged(float indicatorLevel) {
				for (PlayerLevelListener l : levelListeners)
					l.levelChanged(indicatorLevel);
			}
		};

		setEq();
	}

	@Override
	public void close() {
		compr.release();
		super.close();
	}

	public void setCompr(int db) {
		compr.setComprLevel(db);
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

	boolean plus10;

	public void setPlus10(boolean plus10) {
		this.plus10 = plus10;
	}

	private void setEq() {
		float volume = comprGain - compr.getComprLevel();
		System.out.println("vol " + volume);
		setVolume(volume);

		// setEq(speedKMH * dBPer100 / 100 - dBPer100 * 1.2f);

		float eq = dBPer100 * (speedKMH - 120) / 100;
		if (plus10)
			eq += 10;

		setEq(eq);
	}

	@Override
	protected void resetGain() {
		compr.resetGain();
	}

}
