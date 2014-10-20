package kvv.goniometer.ui.mainpage;

import javax.swing.JComponent;

public interface IMainView {
	void setParams(float minX, float maxX, float stepX, float minY, float maxY,
			float stepY);

	void updateData(Float polar);

	JComponent getComponent();

}
