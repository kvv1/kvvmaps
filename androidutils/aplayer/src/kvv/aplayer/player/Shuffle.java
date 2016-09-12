package kvv.aplayer.player;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class Shuffle {

	private static Random r = new Random();

	public static <T> long shuffle(List<T> ar) {
		long seed = r.nextLong();
		shuffle(ar, seed);
		return seed;
	}

	public static <T> void shuffle(List<T> ar, long seed) {
		Random rnd = new Random(seed);
		for (int i = ar.size() - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			T a = ar.get(index);
			ar.set(index, ar.get(i));
			ar.set(i, a);
		}
	}

	public static Random getTodayRandom(int salt) {
		Calendar calendar = Calendar.getInstance();
		int n = (calendar.get(Calendar.YEAR) << 16)
				+ (calendar.get(Calendar.MONTH) << 8)
				+ calendar.get(Calendar.DATE);
		Random rnd = new Random(n + salt);
		return rnd;
	}

	public static int getRandom(Random rnd, int min, int max) {
		return rnd.nextInt(max - min) + min;
	}

}
