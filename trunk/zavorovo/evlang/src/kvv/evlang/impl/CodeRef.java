package kvv.evlang.impl;

public class CodeRef {
	public short off;
	public short len;
	private final Context context;

	public CodeRef(Code code) {
		this.context = code.context;
		this.off = context.codeArr.size();
		context.codeArr.addAll(code);
		this.len = code.size();
	}
}