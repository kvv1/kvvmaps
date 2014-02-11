package kvv.evlang.rt;

public class StackImpl implements Stack {
	private short[] data = new short[100];
	private short sp = (short) data.length;

	public boolean isEmpty() {
		return sp == data.length;
	}

	public short pop() {
		return data[sp++];
	}

	@Override
	public short pick(int n) {
		return data[sp + n];
	}

	public void push(int n) {
		data[--sp] = (short) n;
	}

	public short getSP() {
		return sp;
	}

	public void setSP(short s) {
		sp = s;
	}

	public int depth() {
		return data.length - sp;
	}

	public short getAt(int base, int off) {
		return data[base + off];
	}

	public void setAt(int base, int off, short n) {
		data[base + off] = n;
	}

}
