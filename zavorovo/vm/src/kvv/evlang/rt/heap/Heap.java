package kvv.evlang.rt.heap;

public interface Heap {
	public int alloc(int typeIdx_arrSize, boolean array, boolean objArray);

	public short get(int a, int idx);

	public void set(int a, int idx, int val);

	public boolean mark(int a);
	public void markClosure();

	public void sweep();

	public int getArraySize(int a);

	public int getTypeIdx(int a);

}
