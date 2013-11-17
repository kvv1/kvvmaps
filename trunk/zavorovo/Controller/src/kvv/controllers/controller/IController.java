package kvv.controllers.controller;

import java.io.IOException;
import java.util.Map;

import kvv.controllers.register.AllRegs;


public interface IController {
	public class Statistics {
	}

//	public enum REG_TYPE {
//		COIL,
//		DISCRETE_INPUT,
//		PHYSICAL_INPUT,
//		STORAGE
//	}
	
	public void setReg(int addr, int reg, int val) throws IOException;

	public int getReg(int addr, int reg) throws IOException;

	public int[] getRegs(int addr, int reg, int n) throws IOException;

	public AllRegs getAllRegs(int addr) throws IOException;

	public void upload(int addr, byte[] data) throws IOException;

	public void close();

	void vmInit(int addr) throws IOException;

	Map<Integer, Statistics> getStatistics();

	void clearStatistics();
}
