package kvv.controllers.controller;

import java.io.IOException;

import kvv.controller.register.Statistics;

public interface IController {

	void setModbusLine(ModbusLine modbusLine);

	void setReg(int addr, int reg, int val) throws IOException;

	void setRegs(int addr, int reg, int... val) throws IOException;

	Integer getReg(int addr, int reg) throws IOException;

	Integer[] getRegs(int addr, int reg, int n) throws IOException;

	Statistics getStatistics(boolean clear) throws IOException;

	void uploadApp(int addr, byte[] image) throws IOException;

	Integer hello(int addr) throws IOException;
	
	void reset(int addr) throws IOException;

	void close();
}
