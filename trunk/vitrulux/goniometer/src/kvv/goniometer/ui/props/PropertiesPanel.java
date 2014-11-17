package kvv.goniometer.ui.props;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import kvv.goniometer.ui.utils.FlowWrapper;

@SuppressWarnings("serial")
public class PropertiesPanel extends JPanel {

	private final JPanel propsPanel = new JPanel();
	private final JPanel propsPanelExt = new JPanel();
	public Properties properties;

	public void onChanged() {
	}

	public PropertiesPanel() {

		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		propsPanel.setLayout(new BoxLayout(propsPanel, BoxLayout.PAGE_AXIS));
		propsPanelExt.setLayout(new BoxLayout(propsPanelExt,
				BoxLayout.PAGE_AXIS));
		propsPanelExt.setVisible(false);

		final JCheckBox cb = new JCheckBox("Расширенные настройки");
		cb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				propsPanelExt.setVisible(cb.isSelected());
			}
		});

		panel.add(propsPanel);
		panel.add(new FlowWrapper(FlowLayout.LEFT, 0, 0, cb));
		panel.add(propsPanelExt);

		super.add(panel, BorderLayout.PAGE_START);

		JButton save = new JButton("Сохранить");

		super.add(new FlowWrapper(FlowLayout.RIGHT, save),
				BorderLayout.PAGE_END);

		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Properties properties = new Properties();
					properties.putAll(PropertiesPanel.this.properties);
					for (int i = 0; i < propsPanel.getComponentCount(); i++)
						((PropertyPanel) propsPanel.getComponent(i))
								.put(properties);
					for (int i = 0; i < propsPanelExt.getComponentCount(); i++)
						((PropertyPanel) propsPanelExt.getComponent(i))
								.put(properties);
					FileWriter wr = new FileWriter("settings.prop");
					properties.store(wr, "");
					wr.close();
					PropertiesPanel.this.properties = properties;
					onChanged();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

	}

	Map<String, PropertyPanel> map = new HashMap<>();

	public void add(PropertyPanel propertyPanel) {
		propsPanel.add(propertyPanel);
		map.put(propertyPanel.propName, propertyPanel);
	}

	public void addExt(PropertyPanel propertyPanel) {
		propsPanelExt.add(propertyPanel);
		map.put(propertyPanel.propName, propertyPanel);
	}

	public void addHidden(PropertyPanel propertyPanel) {
		map.put(propertyPanel.propName, propertyPanel);
	}

	public String get(String name) {
		PropertyPanel panel = map.get(name);

		return properties.getProperty(name, panel.defaultValue);

	}

	public void load() {
		properties = new Properties();
		try {
			properties.load(new FileReader("settings.prop"));
			for (int i = 0; i < propsPanel.getComponentCount(); i++)
				((PropertyPanel) propsPanel.getComponent(i)).get(properties);
			for (int i = 0; i < propsPanelExt.getComponentCount(); i++)
				((PropertyPanel) propsPanelExt.getComponent(i)).get(properties);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
