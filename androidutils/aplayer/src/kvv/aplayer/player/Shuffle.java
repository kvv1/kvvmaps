package kvv.aplayer.player;

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

}
