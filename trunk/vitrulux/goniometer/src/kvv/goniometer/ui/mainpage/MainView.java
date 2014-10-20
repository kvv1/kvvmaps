package kvv.goniometer.ui.mainpage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import kvv.goniometer.ui.mainpage.DataSet.Data;

@SuppressWarnings("serial")
public class MainView extends JPanel implements IMainView {
	private static final int WIDTH = 400;
	private static final int HEIGHT = 400;

	private final DiagramView diagramView;
	JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, 0);

	private float minX;
	private float maxX;
	private float stepX;
	private float minY;
	private float maxY;
	private float stepY;

	DataSet dataSet;

	private final JTable table;
	private final SpectrumView spectrum = new SpectrumView();

	DefaultTableModel model = new DefaultTableModel(new Object[] { "Азимут",
			"E", "x", "y", "T" }, 0) {
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	private final ListSelectionListener listSelectionListener = new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			int[] selectedRow = table.getSelectedRows();
			if (selectedRow.length > 0) {
				float az = (float) table.getValueAt(selectedRow[0], 0);

				for (Data d : dataSet.getData()) {
					if (d.x == az && d.y == polar) {
						spectrum.setData(d.value);
					}
				}
			} else {
				spectrum.setData(null);
			}
		}
	};

	private float polar;

	public MainView(final DataSet dataSet) {
		this.dataSet = dataSet;
		this.diagramView = new DiagramView(dataSet);
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
				float g = minY + v * stepY;
				setPolar(g);
			}
		});

		sliderPanel.add(new JLabel("Полярный угол"), BorderLayout.PAGE_START);
//		sliderPanel.setAlignmentX(LEFT_ALIGNMENT);
		sliderPanel.add(slider);
		sliderPanel.add(slider, BorderLayout.WEST);
		add(sliderPanel);

		table = new JTable(model);
		table.setFillsViewportHeight(true);
		table.getTableHeader().setReorderingAllowed(false); 

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT - 100));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		panel.add(scrollPane);
		panel.add(Box.createVerticalGlue());
		spectrum.setPreferredSize(new Dimension(WIDTH, 90));
		panel.add(spectrum);

		add(panel);

		ListSelectionModel cellSelectionModel = table.getSelectionModel();
		cellSelectionModel
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		cellSelectionModel.addListSelectionListener(listSelectionListener);

		setPolar(0);
	}

	void updateTable() {
		while (model.getRowCount() > 0)
			model.removeRow(model.getRowCount() - 1);
		for (Data d : dataSet.getData()) {
			if (d.y == this.polar)
				model.addRow(new Object[] { d.x, (float) d.value.e / 10,
						d.value.x, d.value.y, d.value.t });
		}
	}

	void setPolar(float polar) {
		this.polar = polar;
		diagramView.setPolar(polar);
		updateTable();
	}

	@Override
	public void updateData(Float polar) {
		diagramView.updateData(polar);
		if (polar == null || polar == this.polar) {
			updateTable();
		}
	}

	@Override
	public void setParams(float minX, float maxX, float stepX, float minY,
			float maxY, float stepY) {

		this.minX = minX;
		this.maxX = maxX;
		this.stepX = stepX;
		this.minY = minY;
		this.maxY = maxY;
		this.stepY = stepY;

		slider.setMinimum(0);
		slider.setMaximum((int) ((maxY - minY) / stepY));
		slider.setValue(0);

		Dictionary<Integer, JComponent> dictionary = new Hashtable<>();

		int n = 0;
		for (float y = minY; y <= maxY; y += stepY) {
			JLabel label = new JLabel(" " + (int)y);
//			JPanel panel = new JPanel(new BorderLayout(0, 0));
//			panel.setSize(new Dimension(100, 50/*panel.getPreferredSize().height*/));
//			panel.add(label, BorderLayout.PAGE_START);
			dictionary.put(n++, label);
		}

		slider.setLabelTable(dictionary);

		setPolar(minY);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

}
