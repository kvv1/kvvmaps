package kvv.controllers.controller;

import java.io.IOException;

import kvv.controller.register.AllRegs;
import kvv.controller.register.Rule;
import kvv.controller.register.Statistics;

public interface IController {

	void setModbusLine(ModbusLine modbusLine);

	void setReg(int addr, int reg, int val) throws IOException;

	void setRegs(int addr, int reg, int... val) throws IOException;

	int getReg(int addr, int reg) throws IOException;

	int[] getRegs(int addr, int reg, int n) throws IOException;

	AllRegs getAllRegs(int addr) throws IOException;
	
	Statistics getStatistics(boolean clear) throws IOException;

	void uploadApp(int addr, byte[] image) throws IOException;

	Integer hello(int addr) throws IOException;

	Rule[] getRules(int addr) throws IOException;

	void setRules(int addr, Rule[] rules) throws IOException;
	
	void close();
}
