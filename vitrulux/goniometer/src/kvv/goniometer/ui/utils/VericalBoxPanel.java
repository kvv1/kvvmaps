package kvv.goniometer.ui.utils;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class VericalBoxPanel extends JPanel {

	public VericalBoxPanel(Component... components) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		for(Component c : components)
			add(c);
	}
}
