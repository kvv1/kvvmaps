package kvv.controller.register;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Rule implements Serializable {
	public boolean en;
	public int srcReg;
	public int srcVal;
	public Operation op;
	public int dstReg;
	public int dstVal;
}
