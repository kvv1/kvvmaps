package kvv.heliostat.server.motor;

import kvv.heliostat.shared.MotorState.MotorRawSimState;


public interface MotorRaw {
	void stepSim(int ms);
	
	void init();
	
//	void setStepNumber(int cnt);
//	int getStepsCounter();
	
//	void setDir(boolean dir);
//	boolean getDir();
	
	void moveIn1N(int cnt);
	void moveIn2N(int cnt);

	int getPosition();
	void setPosition(int pos);
	
	boolean getIn1();
	boolean getIn2();

	void stop();

	MotorRawSimState getState();
	void setFast(boolean b);
	
	
	
}
