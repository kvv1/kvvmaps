package kvv.heliostat.server.envir.controller;

import java.io.IOException;


public interface ModbusLine {
	byte[] handle(int addr, byte[] pdu) throws IOException;
	Statistics getStatistics(boolean clear);

	void close();

}
