package kvv.evlang.rt;


public interface Heap {
	
	public short alloc(int typeIdx);
	
	public short get(int a, int off);
	
	public void set(int a, int off, int val);
	
	public void mark(int a);
	
	public void sweep();

}
