package ijTest.abstracteditor;


import java.awt.Component;

public interface IView {
	void setDocument(IDocument doc);
	Component getComponent();
}
