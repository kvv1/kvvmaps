package kvv.kvvmap.common.pacemark;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kvv.kvvmap.adapter.LocationX;

public class PathIO {
	public static List<LocationX> readPlaceMarks(BufferedReader rd,
			String startline) throws IOException {
		List<LocationX> pms = new ArrayList<LocationX>();

		for (;;) {
			String line;
			if (startline != null) {
				line = startline;
				startline = null;
			} else {
				line = rd.readLine();
			}

			if (line == null)
				break;
			String[] tokens = split(line, ' ');
			if (tokens.length <= 0)
				continue;
			double lon = 0;
			double lat = 0;
			double alt = 0;
			float speed = 0;
			long time = 0;
			String pmname = null;
			for (String token : tokens) {
				String[] namevals = split(token, '=');
				if (namevals.length == 2) {
					String name = namevals[0];
					String value = namevals[1];
					if (name.equals("lon"))
						lon = Double.parseDouble(value.replace(',', '.'));
					if (name.equals("lat"))
						lat = Double.parseDouble(value.replace(',', '.'));
					if (name.equals("alt"))
						alt = Integer.parseInt(value);
					if (name.equals("name"))
						pmname = value;
					if (name.equals("speed"))
						speed = Float.parseFloat(value);
					if (name.equals("time"))
						time = Long.parseLong(value);
				}
			}
			LocationX pm = new LocationX(lon, lat, alt, 0, speed, time);
			if (pmname != null)
				pm.name = pmname;
			pms.add(pm);
		}
		return pms;
	}

	public static String[] split(String str, char c) {
		String[] arr = new String[10];
		int n = 0;
		int idx = 0;
		for (;;) {
			int idx1 = str.indexOf(c, idx);
			if (idx1 == -1)
				idx1 = str.length();

			if (idx1 != idx)
				arr[n++] = str.substring(idx, idx1);

			if (idx1 == str.length())
				break;
			else
				idx = idx1 + 1;
		}
		String[] res = new String[n];
		System.arraycopy(arr, 0, res, 0, n);

		return res;
	}

}
