package kvv.heliostat.server.controller;

import java.io.IOException;
import java.io.InputStream;

public interface IController {

	void setReg(int addr, int reg, int val) throws IOException;

	int getReg(int addr, int reg) throws IOException;

	int[] getRegs(int addr, int reg, int n) throws IOException;

	void upload(int addr, byte[] data) throws IOException;

	void vmInit(int addr) throws IOException;

	String getStatistics(boolean clear) throws IOException;

	void uploadAppHex(int addr, InputStream is) throws IOException;

	Integer hello(int addr) throws IOException;

	void close();
}
