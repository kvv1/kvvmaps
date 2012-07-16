package kvv.controllers.server.rs485;

import java.util.Map;

public interface IController {
	public void setReg(int addr, int reg, int val) throws Exception;
	public int getReg(int addr, int reg) throws Exception;
	public Map<Integer, Integer> getRegs(int addr) throws Exception;
	public void close();

}
