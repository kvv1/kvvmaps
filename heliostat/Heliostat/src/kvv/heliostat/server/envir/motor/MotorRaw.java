package kvv.heliostat.server.envir.motor;

import java.io.IOException;

import kvv.heliostat.client.dto.MotorState;

public interface MotorRaw {
	void stepSim(int ms);

	void init();

	void close();

	void moveIn1N(int cnt) throws IOException;

	void moveIn2N(int cnt) throws IOException;

	int getPosition() throws IOException;

	void clearPosition() throws IOException;

	boolean getIn1() throws IOException;

	boolean getIn2() throws IOException;

	void stop() throws IOException;

	Integer getPosAbs() throws IOException;

	void setFast(boolean b) throws IOException;

	boolean isRunning() throws IOException;
	
	MotorState getState() throws IOException;
}
