package kvv.controllers.client.control.simple;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class ExprContol extends Composite {
	private final VerticalPanel verticalPanel = new VerticalPanel();
	public HorizontalPanel buttonsPanel = new HorizontalPanel();

	Label errMsg = new Label("err");
	TextArea expr = new TextArea();
	Button testButton = new Button("Test");

	protected abstract void testExpr(String e);

	public ExprContol(String expr2, String errMsg2) {
		//verticalPanel.setBorderWidth(1);
		
		expr.setSize("300px", "50px");
		expr.setText(expr2);
		errMsg.setText(errMsg2);
		errMsg.setVisible(errMsg2 != null && !errMsg2.isEmpty());
		
		errMsg.getElement().getStyle().setColor("red");

		verticalPanel.add(expr);
		buttonsPanel.add(testButton);

		verticalPanel.add(errMsg);
		verticalPanel.add(buttonsPanel);

		testButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String e = expr.getText();
				testExpr(e);
			}
		});

		initWidget(verticalPanel);
	}

	public String getText() {
		return expr.getText();
	}

}
