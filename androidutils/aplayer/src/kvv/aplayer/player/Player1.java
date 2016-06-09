package kvv.aplayer.player;

import java.util.List;

import kvv.aplayer.service.Folder;

public abstract class Player1 extends Player0 {

	private final Compressor compr;

	private float comprGain;
	private float dBPer100;
	private float speedKMH;

	@Override
	public void onChanged(OnChangedHint hint) {
		if (compr != null)
			compr.setPlaying(isPlaying());
	}

	public Player1(List<Folder> folders) {
		super(folders);

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
	}

	protected abstract void levelChanged(float indicatorLevel);

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

	private void setEq() {
		setVolume(comprGain - compr.getComprLevel());
		setEq(speedKMH * dBPer100 / 100 - dBPer100 * 1.2f);
	}

	@Override
	protected void resetGain() {
		compr.resetGain();
	}

	public void setVisible(boolean vis) {
		compr.setVisible(vis);
	}
}

