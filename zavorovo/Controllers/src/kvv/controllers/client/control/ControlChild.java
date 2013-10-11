package kvv.controllers.client.control;

import java.util.Set;

import kvv.controllers.register.AllRegs;

public interface ControlChild {
	Set<Integer> getAddrs();
	void refresh(AllRegs result);
}
