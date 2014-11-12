package kvv.goniometer.ui.mainpage;

import javax.swing.JComponent;

import kvv.goniometer.ui.mainpage.DataSet.Data;

public interface IMainView {
	void setParams();

	void updateData(Data data);

	JComponent getComponent();

}
