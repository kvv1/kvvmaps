package kvv.heliostat.server.controller;


public interface ModbusLine {
	byte[] handle(int addr, byte[] pdu);
	Statistics getStatistics(boolean clear);

	void close();

}
