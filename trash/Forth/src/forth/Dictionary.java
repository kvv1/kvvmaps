package forth;

import java.util.LinkedHashMap;
import java.util.Map;

public class Dictionary {
	private Map<String, Word> words = new LinkedHashMap<String, Word>();
	private Word latest;
	
	public void add(Word w) {
		words.put(w.name, w);
		latest = w;
	}
	
	public Word find(String name) {
		return words.get(name); 
	}
	
//	public int find(String name) {
//		for(int i = words.size() - 1; i >= 0; i--)
//			if(words.get(i).name.equals(name))
//				return i;
//		return -1;
//	}
//
//	public Word get(int i) {
//		return words.get(i);
//	}

	public Word latest() {
		return latest;
	}

	public void print() {
		System.out.println();
		for(Word w : words.values())
			System.out.print(w.name + " " + w.codeIdx + " ");
		System.out.println();
	}

}
