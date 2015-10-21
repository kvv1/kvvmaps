package kvv.heliostat.client.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Weather implements Serializable {
	public int sunrize;
	public int sunset;
	public int ptsPerHour;
	public int firstDay;

	public boolean[][] values;

	public Weather() {
	}

	public Weather(int sunrize, int sunset, int ptsPerHour, int firstDay,
			boolean[][] values) {
		this.sunrize = sunrize;
		this.sunset = sunset;
		this.ptsPerHour = ptsPerHour;
		this.firstDay = firstDay;
		this.values = values;
	}

	public int getPts() {
		return (sunset - sunrize) * ptsPerHour;
	}

	public int t2p(double t) {
		return (int) ((t + 1.0 / ptsPerHour / 2 - sunrize) * ptsPerHour);
	}

	public double p2t(int p) {
		return sunrize + (double)p / ptsPerHour;
	}
	
	public int d2off(int d) {
		return (d + 365 - firstDay) % 365;
	}

}
