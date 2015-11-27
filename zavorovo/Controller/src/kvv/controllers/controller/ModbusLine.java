package kvv.controllers.controller;

import java.io.IOException;

import kvv.controller.register.Statistics;


public interface ModbusLine {
	void setTimeout(int addr, int timeout);
	byte[] handle(int addr, byte[] pdu) throws IOException;
	Statistics getStatistics(boolean clear);

	void close();

}
