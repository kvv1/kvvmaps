package kvv.aplayer.service;

import java.util.List;

import kvv.aplayer.player.Player.OnChangedHint;

public class PlayerX {

	List<Folder> folders;
	
	public PlayerX(List<Folder> folders) {
		this.folders = folders;
		// TODO Auto-generated constructor stub
	}

	public void onChanged(OnChangedHint hint) {
		// TODO Auto-generated method stub
		
	}

	protected void levelChanged(float indicatorLevel) {
		// TODO Auto-generated method stub
		
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	boolean playing;
	
	public void pause() {
		playing = false;
		onChanged(OnChangedHint.POSITION);
	}

	public boolean isPlaying() {
		return playing;
	}

	public void play() {
		playing = true;
		onChanged(OnChangedHint.POSITION);
	}

	public void prev() {
		// TODO Auto-generated method stub
		
	}

	public void next() {
		// TODO Auto-generated method stub
		
	}

	public void setGain(int int1) {
		// TODO Auto-generated method stub
		
	}

	public void setCompr(int i) {
		// TODO Auto-generated method stub
		
	}

	public void setDbPer100(float f) {
		// TODO Auto-generated method stub
		
	}

	public List<Folder> getFolders() {
		return folders;
	}

	public int getCurrentFolder() {
		return 0;
	}

	public void toFolder(int position, int curFile, int curPos) {
		// TODO Auto-generated method stub
		
	}

	public void makeRandom(int position) {
		// TODO Auto-generated method stub
		
	}

	public void toFile(int position) {
		// TODO Auto-generated method stub
		
	}

	public int getDuration() {
		// TODO Auto-generated method stub
		return 10;
	}

	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void seekTo(int f) {
		// TODO Auto-generated method stub
		
	}

	public void seek(int seekStep) {
		// TODO Auto-generated method stub
		
	}

	public int getFile() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getGain() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setVisible(boolean vis) {
		// TODO Auto-generated method stub
		
	}

	public void setSpeedKMH(float speed) {
		// TODO Auto-generated method stub
		
	}

}
