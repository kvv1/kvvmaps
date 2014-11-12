package kvv.goniometer.ui.mainpage;

import javax.swing.JComponent;

import kvv.goniometer.ui.mainpage.DataSet.Data;

public interface IMainView {
	void propsChanged();

	void updateData(Data data);

	JComponent getComponent();
}
