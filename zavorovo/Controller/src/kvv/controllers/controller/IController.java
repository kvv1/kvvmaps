package kvv.controllers.controller;

import java.io.IOException;

import kvv.controllers.register.AllRegs;

public interface IController {
	public void setReg(int addr, int reg, int val) throws Exception;
	public int getReg(int addr, int reg) throws Exception;
	public int[] getRegs(int addr, int reg, int n) throws Exception;
	public AllRegs getAllRegs(int addr) throws Exception;
	public void upload(int addr, byte[] data) throws IOException;
	public void close();

}
