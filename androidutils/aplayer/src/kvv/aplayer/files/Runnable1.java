package kvv.aplayer.files;

public abstract class Runnable1 implements Runnable {
	@Override
	public final void run() {
		try {
			run1();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract void run1();
}