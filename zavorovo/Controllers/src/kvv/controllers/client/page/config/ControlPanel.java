package kvv.controllers.client.page.config;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class ControlPanel extends Composite {
	HorizontalPanel hp = new HorizontalPanel();

	public ControlPanel(final Tree tree, final TreeItem item) {
		Button up = new Button("Up");
		Button down = new Button("Down");
		Button del = new Button("Del");

		hp.add(up);
		hp.add(down);
		hp.add(del);

		del.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// if (Window.confirm("Удалить?"))
				item.remove();
			}
		});

		up.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				TreeItem parent = item.getParentItem();
				if (parent != null) {
					int idx = parent.getChildIndex(item);
					if (idx > 0) {
						parent.removeItem(item);
						parent.insertItem(idx - 1, item);
					}
				} else {
					int idx = 0;
					for (int i = 0; i < tree.getItemCount(); i++)
						if (tree.getItem(i) == item)
							idx = i;
					if (idx > 0) {
						tree.removeItem(item);
						tree.insertItem(idx - 1, item);
					}
				}
			}
		});

		down.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				TreeItem parent = item.getParentItem();
				if (parent != null) {
					int idx = parent.getChildIndex(item);

					if (idx < parent.getChildCount() - 1) {
						parent.removeItem(item);
						parent.insertItem(idx + 1, item);
					}
				} else {
					int idx = 0;
					for (int i = 0; i < tree.getItemCount(); i++)
						if (tree.getItem(i) == item)
							idx = i;
					if (idx < tree.getItemCount() - 1) {
						tree.removeItem(item);
						tree.insertItem(idx + 1, item);
					}
				}
			}
		});

		initWidget(hp);
	}
}