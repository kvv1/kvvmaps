package kvv.controllers.client.control.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class EditablePanel<T extends IsWidget> extends Composite {
	private final VerticalPanel panel = new VerticalPanel();
	private final VerticalPanel itemsPanel = new VerticalPanel();
	private final boolean readonly;

	protected abstract T createItem();

	public EditablePanel(boolean readonly, String addButonText) {
		this.readonly = readonly;
		// panel.setSpacing(4);
		panel.add(itemsPanel);
		itemsPanel.setSpacing(4);
		if (!readonly)
			addAddButton(new Button(addButonText, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					add(createItem());
				}
			}));

		initWidget(panel);
	}

	protected void addAddButton(Button button) {
		panel.add(button);
	}

	private HorizontalPanel itemPanel;

	protected void addNavPanel(T widget, NavPanel navPanel) {
		itemPanel.add(navPanel);
	}

	public void add(T widget) {
		itemPanel = new HorizontalPanel();
		itemPanel.add(widget);
		if (!readonly)
			addNavPanel(widget, new NavPanel(itemPanel));
		itemsPanel.add(itemPanel);
	}

	@SuppressWarnings("unchecked")
	public List<T> getItems() {
		List<T> res = new ArrayList<>();
		for (int i = 0; i < itemsPanel.getWidgetCount(); i++)
			res.add((T) ((HorizontalPanel) itemsPanel.getWidget(i))
					.getWidget(0));
		return res;
	}

	public void clear() {
		itemsPanel.clear();
	}

	public class NavPanel extends Composite {
		private HorizontalPanel panel = new HorizontalPanel();

		public NavPanel(final Widget widget) {
			panel.add(new Button("Up", new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					int idx = itemsPanel.getWidgetIndex(widget);
					if (idx > 0) {
						itemsPanel.remove(widget);
						itemsPanel.insert(widget, idx - 1);
						// updateUI();
					}
				}
			}));
			panel.add(new Button("Down", new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					int idx = itemsPanel.getWidgetIndex(widget);
					if (idx < itemsPanel.getWidgetCount() - 1) {
						itemsPanel.remove(widget);
						itemsPanel.insert(widget, idx + 1);
						// updateUI();
					}
				}
			}));
			panel.add(new Button("Del", new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					itemsPanel.remove(widget);
				}
			}));
			initWidget(panel);
		}
	}

	class Space extends Panel {

		@Override
		public Iterator<Widget> iterator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean remove(Widget child) {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
