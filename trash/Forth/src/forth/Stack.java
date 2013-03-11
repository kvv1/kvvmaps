package forth;

public class Stack {
	private static final int DEPTH = 100;
	private int[] data = new int[DEPTH];
	private int ptr = DEPTH;
	public void push(int n) {
		data[--ptr] = n;
	}
	public int pop() {
		return data[ptr++];
	}
	public int at(int n) {
		return data[ptr + n];
	}
	public void print() {
		System.out.print(" { ");
		for(int i = DEPTH - 1; i >= ptr; i--)
			System.out.print(data[i] + " ");
		System.out.print("} ");
	}
}
