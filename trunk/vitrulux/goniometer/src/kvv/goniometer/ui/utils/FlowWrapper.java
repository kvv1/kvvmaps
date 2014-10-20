package kvv.goniometer.ui.utils;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FlowWrapper extends JPanel {

	public FlowWrapper(int align, Component... components) {
		super(new FlowLayout(align));
		for (Component c : components)
			add(c);
	}

	public FlowWrapper(int align, int hgap, int vgap, Component... components) {
		super(new FlowLayout(align, hgap, vgap));
		for (Component c : components)
			add(c);
	}

}
