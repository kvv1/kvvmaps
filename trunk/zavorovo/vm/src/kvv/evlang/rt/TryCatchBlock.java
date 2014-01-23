package kvv.evlang.rt;

public class TryCatchBlock {
	public short from;
	public short to;
	public short handler;

	public TryCatchBlock(int from, int to, int handler) {
		this.from = (short) from;
		this.to = (short) to;
		this.handler = (short) handler;
	}
}