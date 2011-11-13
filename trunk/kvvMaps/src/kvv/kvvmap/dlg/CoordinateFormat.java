package kvv.kvvmap.dlg;

import java.util.Locale;

public class CoordinateFormat {

	public static void main(String[] args) {

		String[] dm = formatDM(-10.5004);
		double ll = parseDM(dm[0], dm[1]);
		System.out.println(ll);
		
		String[] dms = formatDMS(-10.5004);
		ll = parseDMS(dms[0], dms[1], dms[2]);
		System.out.println(ll);
		formatDMS(ll);
	}

	public static String[] formatDM(double ll) {
		boolean minus = false;

		if (ll < 0) {
			ll = -ll;
			minus = true;
		}

		int d = (int) ll;
		double m = ll * 60 % 60;

		String[] res = new String[3];
		res[0] = (minus ? "-" : "") + d;
		res[1] = String.format(Locale.ENGLISH, "%2.2f", m);

//		System.out.println(res[0] + " " + res[1]);
		return res;
	}

	public static String[] formatDMS(double ll) {
		boolean minus = false;

		if (ll < 0) {
			ll = -ll;
			minus = true;
		}

		int d = (int) ll;
		int m = (int) (ll * 60) % 60;
		int s = (int) (ll * 3600) % 60;

		String[] res = new String[3];
		res[0] = (minus ? "-" : "") + d;
		res[1] = Integer.toString(m);
		res[2] = Integer.toString(s);

//		System.out.println(res[0] + " " + res[1] + " " + res[2]);
		return res;
	}

	public static double parseDM(String d, String m) {
		if (d.startsWith("--")) {
			throw new NumberFormatException();
		}

		boolean minus = false;

		if (d.startsWith("-")) {
			d = d.substring(1);
			minus = true;
		}

		double res = Integer.parseInt(d) + Double.parseDouble(m) / 60;
		if (minus)
			res = -res;

		return res;
	}

	public static double parseDMS(String d, String m, String s) {
		if (d.startsWith("--")) {
			throw new NumberFormatException();
		}

		boolean minus = false;

		if (d.startsWith("-")) {
			d = d.substring(1);
			minus = true;
		}

		double res = Integer.parseInt(d) + (double) Integer.parseInt(m) / 60
				+ Double.parseDouble(s) / 3600;
		if (minus)
			res = -res;

		return res;
	}
}
