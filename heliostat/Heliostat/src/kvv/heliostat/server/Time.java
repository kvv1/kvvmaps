package kvv.heliostat.server;

import kvv.heliostat.client.dto.DayTime;


public interface Time {
	DayTime getTime();

	void setTime(double time);

	void setDay(int day);
}
