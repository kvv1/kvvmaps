package kvv.evlang.rt.heap;

import kvv.evlang.rt.Stack;

public class Stack2 implements Stack {

	private final Heap heap;

	private int a;
	private int off;

	public Stack2(Heap heap) {
		this.heap = heap;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public short pop() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short pick(int n) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void push(int n) {
		// TODO Auto-generated method stub

	}

	@Override
	public short getSP() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSP(short s) {
		// TODO Auto-generated method stub

	}

	@Override
	public int depth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getAt(int base, int off) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAt(int base, int off, short n) {
		// TODO Auto-generated method stub

	}

}
