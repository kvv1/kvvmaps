package kvv.controllers.client.control;

import java.util.Set;

public interface ControlChild {
	Set<Integer> getAddrs();
	void refresh(AllRegs result);
}
