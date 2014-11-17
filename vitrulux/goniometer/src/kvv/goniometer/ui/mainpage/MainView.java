package kvv.goniometer.ui.mainpage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import kvv.goniometer.Props;
import kvv.goniometer.ui.mainpage.DataSet.Data;
import kvv.goniometer.ui.props.Prop;
import kvv.goniometer.ui.utils.VericalBoxPanel;

@SuppressWarnings("serial")
public class MainView extends JPanel implements IMainView {
	private static final int WIDTH = 500;
	private static final int HEIGHT = 500;

	private final DiagramView diagramView;
	private final JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
	private final TableView tableView;

	private float minPrim;
	private float maxPrim;
	private float stepPrim;

	private final JRadioButton primLabelAzim = new JRadioButton(
			DIR.AZIMUTH.text);
	private final JRadioButton primLabelPol = new JRadioButton(DIR.POLAR.text);

	private DIR primDir = DIR.AZIMUTH;

	private float prim;
	private final Props props;

	public MainView(DataSet dataSet, Props props) {
		this.props = props;

		try {
			DIR scanDir = DIR.valueOf(props.get(Prop.SCAN_DIR));
			primDir = DIR.values()[(scanDir.ordinal() + 1)
					% DIR.values().length];
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (primDir == DIR.AZIMUTH)
			primLabelAzim.setSelected(true);
		else
			primLabelPol.setSelected(true);

		this.diagramView = new DiagramView(dataSet) {
			@Override
			protected DIR getDir() {
				return MainView.this.primDir;
			}

			@Override
			protected float getPrim() {
				return MainView.this.prim;
			}
		};

		diagramView.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		JPanel sliderPanel = new JPanel(new BorderLayout());
		sliderPanel.setPreferredSize(new Dimension(100, HEIGHT));

		slider.setPaintLabels(true);

		Dictionary<Integer, JComponent> dictionary = new Hashtable<>();
		dictionary.put(0, new JLabel("          "));

		slider.setLabelTable(dictionary);

		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int v = slider.getValue();
				prim = minPrim + v * stepPrim;
				updateData(null);
			}
		});

		sliderPanel.add(new VericalBoxPanel(primLabelAzim, primLabelPol),
				BorderLayout.PAGE_START);
		// sliderPanel.add(primLabel, BorderLayout.PAGE_START);
		sliderPanel.add(slider, BorderLayout.WEST);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(primLabelAzim);
		buttonGroup.add(primLabelPol);

		primLabelAzim.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				primDir = DIR.AZIMUTH;
				propsChanged();
			}
		});

		primLabelPol.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				primDir = DIR.POLAR;
				propsChanged();
			}
		});

		// scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT
		// - SPECTRUM_HEIGHT - 10));

		tableView = new TableView(dataSet, props, WIDTH, HEIGHT) {
			@Override
			protected DIR getDir() {
				return MainView.this.primDir;
			}

			@Override
			protected float getPrim() {
				return MainView.this.prim;
			}
		};

		add(sliderPanel);
		add(diagramView);
		add(tableView);

		propsChanged();
	}

	private DIR getDir() {
		return primDir;
	}

	@Override
	public void updateData(Data data) {
		if (data == null || data.getPrim(getDir()) == this.prim) {
			tableView.updateData();
			diagramView.updateData();
		}
	}

	@Override
	public void propsChanged() {
		if (getDir() == DIR.AZIMUTH) {
			minPrim = props.getFloat(Prop.X_START_DEGREES);
			maxPrim = props.getFloat(Prop.X_END_DEGREES);
			stepPrim = props.getFloat(Prop.X_STEP_DEGREES);
		} else {
			minPrim = props.getFloat(Prop.Y_START_DEGREES);
			maxPrim = props.getFloat(Prop.Y_END_DEGREES);
			stepPrim = props.getFloat(Prop.Y_STEP_DEGREES);
		}

		tableView.propsChanged();

		slider.setMinimum(0);
		slider.setMaximum((int) ((maxPrim - minPrim) / stepPrim));
		slider.setValue(0);

		Dictionary<Integer, JComponent> dictionary = new Hashtable<>();

		int n = 0;
		for (float y = minPrim; y <= maxPrim; y += stepPrim) {
			JLabel label = new JLabel(" " + (int) y);
			dictionary.put(n++, label);
		}

		slider.setLabelTable(dictionary);

		updateData(null);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

}
