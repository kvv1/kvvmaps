package kvv.heliostat.server.envir.controller;


public interface ModbusLine {
	byte[] handle(int addr, byte[] pdu);
	Statistics getStatistics(boolean clear);

	void close();

}
