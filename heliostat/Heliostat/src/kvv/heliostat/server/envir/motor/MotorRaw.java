package kvv.heliostat.server.envir.motor;



public interface MotorRaw {
	void stepSim(int ms);
	
	void init();
	
	void moveIn1N(int cnt);
	void moveIn2N(int cnt);

	int getPosition();
	void clearPosition();
	
	boolean getIn1();
	boolean getIn2();

	void stop();

	int getPosAbs();
	void setFast(boolean b);
}
