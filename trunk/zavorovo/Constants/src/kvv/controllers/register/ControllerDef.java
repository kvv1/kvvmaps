package kvv.controllers.register;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ControllerDef implements Serializable{
	
	public Integer regEEPROM0;
	public Integer regRAM0;
	public Integer lastReg;
	public Integer lastEEReg;
	
	public Integer regVmOnOff;
	public Integer regVmState;
	
	public int[] relaysBitMapping;
	public int[] regs;
	public int[] relayRegsMul;
	
}
