package kvv.goniometer.ui.mainpage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import kvv.goniometer.Props;
import kvv.goniometer.ui.mainpage.DataSet.Data;
import kvv.goniometer.ui.props.Prop;
import kvv.goniometer.ui.utils.VericalBoxPanel;

@SuppressWarnings("serial")
public class MainView extends JPanel implements IMainView {
	private static final int WIDTH = 400;
	private static final int HEIGHT = 400;

	private static final int SPECTRUM_HEIGHT = 150;

	private final DiagramView diagramView;
	private final JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, 0);

	private float minPrim;
	private float maxPrim;
	private float stepPrim;

	private DataSet dataSet;

	private final JTable table;
	private final SpectrumView spectrum;

	private JLabel primLabel = new JLabel();
	private JRadioButton primLabelAzim = new JRadioButton(DIR.AZIMUTH.text,
			true);
	private JRadioButton primLabelPol = new JRadioButton(DIR.POLAR.text);

	private DIR primDir = DIR.AZIMUTH;

	// private final DefaultTableModel model = new DefaultTableModel(new
	// Object[] {
	// "Азимут", "E", "x", "y", "T" }, 0) {
	// public boolean isCellEditable(int row, int column) {
	// return false;
	// }
	// };

	private final ListSelectionListener listSelectionListener = new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			int[] selectedRow = table.getSelectedRows();
			if (selectedRow.length > 0) {
				float az = (float) table.getValueAt(selectedRow[0], 0);

				for (Data d : dataSet.getData()) {
					if (d.getSec(getDir()) == az && d.getPrim(getDir()) == prim) {
						spectrum.setData(d.value);
					}
				}
			} else {
				spectrum.setData(null);
			}
		}
	};

	private float prim;
	private final Props props;

	public MainView(final DataSet dataSet, Props props) {
		this.dataSet = dataSet;
		this.diagramView = new DiagramView(dataSet);
		this.spectrum = new SpectrumView(props);
		this.props = props;

		diagramView.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		add(diagramView);

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
				float g = minPrim + v * stepPrim;
				setPrimary(g);
			}
		});

		sliderPanel.add(new VericalBoxPanel(primLabelAzim, primLabelPol),
				BorderLayout.PAGE_START);
		// sliderPanel.add(primLabel, BorderLayout.PAGE_START);
		sliderPanel.add(slider, BorderLayout.WEST);
		add(sliderPanel);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(primLabelAzim);
		buttonGroup.add(primLabelPol);

		primLabelAzim.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				primDir = DIR.AZIMUTH;
				setParams();
			}
		});

		primLabelPol.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				primDir = DIR.POLAR;
				setParams();
			}
		});

		table = new JTable();
		table.setFillsViewportHeight(true);
		table.getTableHeader().setReorderingAllowed(false);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT
				- SPECTRUM_HEIGHT - 10));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		panel.add(scrollPane);
		panel.add(Box.createVerticalGlue());
		spectrum.setPreferredSize(new Dimension(WIDTH, SPECTRUM_HEIGHT));
		panel.add(spectrum);

		add(panel);

		ListSelectionModel cellSelectionModel = table.getSelectionModel();
		cellSelectionModel
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		cellSelectionModel.addListSelectionListener(listSelectionListener);

		setParams();
	}

	void updateTable() {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		while (model.getRowCount() > 0)
			model.removeRow(model.getRowCount() - 1);
		for (Data d : dataSet.getData()) {
			if (d.getPrim(getDir()) == this.prim)
				model.addRow(new Object[] { d.getSec(getDir()),
						(float) d.value.e / 10, d.value.x, d.value.y, d.value.t });
		}
	}

	private void setPrimary(float prim) {
		this.prim = prim;
		diagramView.setPrimary(getDir(), prim);
		updateTable();
	}

	private DIR getDir() {
		return primDir;
	}

	@Override
	public void updateData(Data data) {
		diagramView.updateData(data);
		if (data == null || data.getPrim(getDir()) == this.prim) {
			updateTable();
		}
	}

	@Override
	public void setParams() {
		if (getDir() == DIR.AZIMUTH) {
			minPrim = props.getFloat(Prop.X_START_DEGREES, 0);
			maxPrim = props.getFloat(Prop.X_END_DEGREES, 0);
			stepPrim = props.getFloat(Prop.X_STEP_DEGREES, 0);

			table.setModel(new DefaultTableModel(new Object[] { DIR.POLAR.text,
					"E", "x", "y", "T" }, 0) {
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
		} else {
			minPrim = props.getFloat(Prop.Y_START_DEGREES, 0);
			maxPrim = props.getFloat(Prop.Y_END_DEGREES, 0);
			stepPrim = props.getFloat(Prop.Y_STEP_DEGREES, 0);

			table.setModel(new DefaultTableModel(new Object[] {
					DIR.AZIMUTH.text, "E", "x", "y", "T" }, 0) {
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
		}

		primLabel.setText(getDir().text);

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

		setPrimary(minPrim);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

}
