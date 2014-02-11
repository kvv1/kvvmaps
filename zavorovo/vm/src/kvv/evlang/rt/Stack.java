package kvv.evlang.rt;

public interface Stack {
	boolean isEmpty();

	short pop();

	short pick(int n);

	void push(int n);

	short getSP();

	void setSP(short s);

	int depth();

	short getAt(int base, int off);

	void setAt(int base, int off, short n);
}