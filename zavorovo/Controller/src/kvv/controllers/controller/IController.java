package kvv.controllers.controller;

import java.io.IOException;
import java.io.InputStream;

import kvv.controller.register.AllRegs;
import kvv.controller.register.Rule;

public interface IController {

	void setReg(int addr, int reg, int val) throws IOException;

	int getReg(int addr, int reg) throws IOException;

	int[] getRegs(int addr, int reg, int n) throws IOException;

	AllRegs getAllRegs(int addr) throws IOException;

	void upload(int addr, byte[] data) throws IOException;

	void vmInit(int addr) throws IOException;

	String getStatistics(boolean clear) throws IOException;

	void uploadAppHex(int addr, InputStream is) throws IOException;

	Integer hello(int addr) throws IOException;

	void close();

	Rule[] getRules(int addr) throws IOException;

	void setRules(int addr, Rule[] rules) throws IOException;
}
