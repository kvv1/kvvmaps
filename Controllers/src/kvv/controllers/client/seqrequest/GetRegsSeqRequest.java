package kvv.controllers.client.seqrequest;

import java.util.Map;

import kvv.controllers.client.ControllersServiceAsync;

public abstract class GetRegsSeqRequest extends SeqRequest<Map<Integer,Integer>>{
	private final int addr;
	private final ControllersServiceAsync controllersService;

	public GetRegsSeqRequest(int addr, ControllersServiceAsync controllersService) {
		this.addr = addr;
		this.controllersService = controllersService;
	}

	@Override
	public void exec() {
		controllersService.getRegs(addr, this);
	}
	
}
