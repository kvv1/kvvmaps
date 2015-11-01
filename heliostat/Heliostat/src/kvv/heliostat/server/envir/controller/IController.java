package kvv.heliostat.server.envir.controller;

import java.io.IOException;

public interface IController {

	void setModbusLine(ModbusLine modbusLine);

	void setReg(int addr, int reg, int val) throws IOException;

	void setRegs(int addr, int reg, int... val) throws IOException;

	int getReg(int addr, int reg) throws IOException;

	int[] getRegs(int addr, int reg, int n) throws IOException;

	Statistics getStatistics(boolean clear) throws IOException;

	void uploadApp(int addr, byte[] image) throws IOException;

	Integer hello(int addr) throws IOException;

	void close();
}
