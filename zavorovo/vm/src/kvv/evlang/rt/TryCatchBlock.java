package kvv.evlang.rt;

public class TryCatchBlock {
	public int from;
	public int to;
	public int handler;

	public TryCatchBlock(int from, int to, int handler) {
		this.from = from;
		this.to = to;
		this.handler = handler;
	}
}