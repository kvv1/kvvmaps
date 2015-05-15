package kvv.heliostat.client.panel;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class DetPanel<L extends Widget, D extends Widget> extends Composite {

	private final Tree tree = new Tree();
	public final L label;
	public final D details;

	@SuppressWarnings("unchecked")
	public DetPanel(String label, D details) {
		this((L) new Label(label), details);
	}

	public DetPanel(L label, D details) {
		this.label = label;
		this.details = details;
		TreeItem item = new TreeItem(label);
		item.addItem(details);
		tree.addItem(item);

		initWidget(tree);
	}

}
