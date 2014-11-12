package kvv.goniometer.ui.mainpage;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import kvv.goniometer.Props;
import kvv.goniometer.ui.mainpage.DataSet.Data;

@SuppressWarnings("serial")
public abstract class TableView extends JPanel {
	private static final int SPECTRUM_HEIGHT = 150;

	private final DataSet dataSet;
	private final Props props;

	private final JTable table;
	private final SpectrumView spectrum;

	protected abstract DIR getDir();
	
	private DIR dir;

	protected abstract float getPrim();

	public TableView(final DataSet dataSet, Props props, int WIDTH, int HEIGHT) {
		this.dataSet = dataSet;
		this.props = props;

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		table = new JTable();
		table.setFillsViewportHeight(true);
		table.getTableHeader().setReorderingAllowed(false);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT - SPECTRUM_HEIGHT - 10));
		add(scrollPane);
		
		add(Box.createVerticalGlue());
		
		spectrum = new SpectrumView(props);
		spectrum.setPreferredSize(new Dimension(WIDTH, SPECTRUM_HEIGHT));
		add(spectrum);

		ListSelectionModel cellSelectionModel = table.getSelectionModel();
		cellSelectionModel
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		cellSelectionModel.addListSelectionListener(listSelectionListener);

	}

	public void updateData() {
		int[] selectedRow = table.getSelectedRows();
		Float sec = null;
		if (selectedRow.length > 0) {
			sec = (float) table.getValueAt(selectedRow[0], 0);
		}

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		while (model.getRowCount() > 0)
			model.removeRow(model.getRowCount() - 1);

		for (Data d : dataSet.getData()) {
			float sec1 = d.getSec(getDir());
			if (d.getPrim(getDir()) == getPrim()) {
				model.addRow(new Object[] { sec1, (float) d.value.e / 10,
						d.value.x, d.value.y, d.value.t });
//				if (sec != null && sec == sec1)
//					table.setRowSelectionInterval(table.getRowCount() - 1,
//							table.getRowCount() - 1);
			}
		}
	}

	private final ListSelectionListener listSelectionListener = new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			int[] selectedRow = table.getSelectedRows();
			if (selectedRow.length > 0) {
				float sec = (float) table.getValueAt(selectedRow[0], 0);

				for (Data d : dataSet.getData()) {
					if (d.getSec(getDir()) == sec
							&& d.getPrim(getDir()) == getPrim()) {
						spectrum.setData(d.value);
					}
				}
			} else {
				spectrum.setData(null);
			}
		}
	};

	public void propsChanged() {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		while (model.getRowCount() > 0)
			model.removeRow(model.getRowCount() - 1);

		if (getDir() == DIR.AZIMUTH) {
			table.setModel(new DefaultTableModel(new Object[] { dir.text,
					"E", "x", "y", "T" }, 0) {
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
		} else {
			table.setModel(new DefaultTableModel(new Object[] {
					DIR.AZIMUTH.text, "E", "x", "y", "T" }, 0) {
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
		}
	}

}
