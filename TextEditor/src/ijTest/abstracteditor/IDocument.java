package ijTest.abstracteditor;


import java.io.File;
import java.io.IOException;

public interface IDocument {
	boolean getChanged();
	void addListener(IDocumentListener frame);
	void removeListener(IDocumentListener frame);
	File getFile();
	void save(File file) throws IOException; // file can be null
}
