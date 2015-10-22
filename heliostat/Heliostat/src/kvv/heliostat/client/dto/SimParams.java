package kvv.heliostat.client.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SimParams implements Serializable {
	public boolean clock;
	public int clockRate = 1;
	public boolean shortDay;
}
