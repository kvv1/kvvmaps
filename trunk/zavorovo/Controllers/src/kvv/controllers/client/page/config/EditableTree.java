package kvv.controllers.client.page.config;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class EditableTree extends Tree {
	public <T> TreeItem addListItem(TreeItem parent, String string,
			final ItemFactory<T> itemFactory, T[] data) {
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(2);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hp.add(new Label(string));
		Button add = new Button("Add");
		hp.add(add);

		final TreeItem item = parent != null ? parent.addItem(hp) : addItem(hp);
		add.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				TreeItem i = createChild(itemFactory, null);
				item.addItem(i);
				item.setState(true);
			}
		});

		if (data != null) {
			for (T t : data) {
				TreeItem i = createChild(itemFactory, t);
				item.addItem(i);
			}
		}

		return item;
	}

	private <T> TreeItem createChild(ItemFactory<T> cFactory, T t) {
		Widget w = cFactory.createWidget(t);
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		TreeItem i = new TreeItem(hp);
		cFactory.createInterior(i, t);
		hp.add(w);
		hp.add(new ControlPanel(EditableTree.this, i));
		return i;
	}

	public <T> T getForm(TreeItem item) {
		return (T) ((HorizontalPanel)(item.getWidget())).getWidget(0);
	}
	
}
