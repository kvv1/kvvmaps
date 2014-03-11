package kvv.controllers.client.page.config;

import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public abstract class ItemFactory<T> {

	public abstract Widget createWidget(T t);

	public abstract void createInterior(TreeItem item, T t);

}
