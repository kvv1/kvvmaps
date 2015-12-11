package kvv.controllers.controller;

import java.io.IOException;
import java.util.List;

import kvv.controller.register.Statistics;


public interface ModbusLine {
	void setTimeout(int addr, int timeout);
	byte[] handle(int addr, byte[] pdu, Integer timeout) throws IOException;
	Statistics getStatistics(boolean clear);

	List<String> getLog();
	void close();

}
