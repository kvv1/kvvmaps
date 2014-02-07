package kvv.evlang.rt.heap;

public interface Heap {
	public short alloc(int typeIdx_arrSize, boolean array, boolean objArray);

	public short get(int a, int off);

	public void set(int a, int off, int val);

	public void mark(int a);

	public void sweep();

	public int getArraySize(int a);

	public int getTypeIdx(int a);

}
