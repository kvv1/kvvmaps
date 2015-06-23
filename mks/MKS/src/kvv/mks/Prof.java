package kvv.mks;

public class Prof {
	public long t;
	String name;
	public Prof(String name) {
		this.name = name;
		t = System.currentTimeMillis();
	}
	public void print() {
		t = System.currentTimeMillis() - t;
		System.out.println(name + ": " + t + " ms");
	}
}