package kvv.goniometer.ui.utils;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class HorizontalBoxPanel extends JPanel {

	public HorizontalBoxPanel(Component... components) {
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		for(Component c : components)
			add(c);
	}
}
