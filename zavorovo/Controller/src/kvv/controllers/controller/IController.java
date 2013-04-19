package kvv.controllers.controller;

import java.io.IOException;
import java.util.Map;

public interface IController {
	public void setReg(int addr, int reg, int val) throws Exception;
	public int getReg(int addr, int reg) throws Exception;
	public Map<Integer, Integer> getRegs(int addr) throws Exception;
	public void upload(int addr, int start, byte[] data) throws IOException;
	public void upload(int addr, byte[] data) throws IOException;
	public void close();

}
