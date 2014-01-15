package kvv.controllers.server.controller;

import java.io.IOException;

import kvv.controllers.controller.IController;
import kvv.controllers.register.AllRegs;
import kvv.controllers.server.Controllers;

public class ControllerWrapperFilter extends ControllerAdapter{

	public ControllerWrapperFilter(Controllers controllers, IController wrapped) {
		super(controllers, wrapped);
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AllRegs getAllRegs(int addr) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	private Integer adjust(int addr, int reg, Integer val) {
		int hysteresis = controllers.getRegister(addr, reg).hysteresis;
		return null;
	}
	
	
	
	public static void main(String[] args) {
		
	}
}
