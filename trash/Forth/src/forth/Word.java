package forth;

public class Word {
	public Word(String name, int codeIdx, boolean immediate) {
		this.name = name;
		this.codeIdx = codeIdx;
		this.immediate = immediate;
	}
	
	boolean immediate;
	final String name;
	final int codeIdx;
	
	public void setImmediate(boolean immed) {
		immediate = immed;
	}
}
