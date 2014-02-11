package kvv.evlang.rt.heap;

public interface Heap {
	int alloc(int typeIdx_arrSize, boolean array, boolean objArray);
	int alloc2(int typeIdx_arrSize, boolean array, boolean objArray);

	short get(int a, int idx);

	void set(int a, int idx, int val);

	int getRawDataOffset(int a);

	short getRaw(int offset, int idx);

	void setRaw(int offset, int idx, int val);

	boolean mark(int a);

	void markClosure();

	void sweep();

	int getArraySize(int a);

	int getTypeIdx(int a);

	void startMark();

}
