package kvv.heliostat.client.view;

import kvv.gwtutils.client.Gap;
import kvv.gwtutils.client.HorPanel;
import kvv.gwtutils.client.TextFieldView;
import kvv.gwtutils.client.VertPanel;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.dto.MotorState;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.Model.Callback1;
import kvv.heliostat.client.model.View;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class MotorRawView extends Composite implements View {

	private static final int WIDTH = 42;

	private VertPanel vertPanel;
	private HorPanel horPanel;

	private void moveRaw(int steps) {
		model.heliostatService.moveRaw(id, steps, new Callback1<Void>(model));
	}

	private Button1 m1000 = new Button1("-1000", WIDTH, new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			moveRaw(-1000);
		}
	});

	private Button1 m100 = new Button1("-100", WIDTH, new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			moveRaw(-100);
		}
	});

	private Button1 m10 = new Button1("-10", WIDTH, new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			moveRaw(-10);
		}
	});

	private Button1 m1 = new Button1("-1", WIDTH, new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			moveRaw(-1);
		}
	});

	private Button1 p1000 = new Button1("+1000", WIDTH, new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			moveRaw(1000);
		}
	});

	private Button1 p100 = new Button1("+100", WIDTH, new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			moveRaw(100);
		}
	});

	private Button1 p10 = new Button1("+10", WIDTH, new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			moveRaw(10);
		}
	});

	private Button1 p1 = new Button1("+1", WIDTH, new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			moveRaw(1);
		}
	});

	private Button stop = new Button("Stop", new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			model.heliostatService.stop(id, new Callback1<Void>(model));
		}
	});

	private Label running = new Label();

	// private Button calibr = new Button("Calibrate", new ClickHandler() {
	// @Override
	// public void onClick(ClickEvent event) {
	// model.heliostatService.calibrate(id, new Callback<Void>());
	// }
	// });

	private Button home = new Button("Home", new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			model.heliostatService.home(id, new Callback1<Void>(model));
		}
	});

	private TextFieldView pos = new TextFieldView("", 0, WIDTH) {
		{
			button.setText("Go");
		}

		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.move(id, Integer.parseInt(text.getText()),
					new Callback1<Void>(model));
		}
	};

	private Label error = new Label();

	private final Model model;
	private final MotorId id;

	public MotorRawView(Model model, MotorId id) {
		this.model = model;
		this.id = id;
		horPanel = new HorPanel(true, 2, m1000, m100, m10, m1, pos, new Gap(4,
				4), p1, p10, p100, p1000, new Gap(4, 4), stop, home, running);
		vertPanel = new VertPanel(error, new Gap(4, 4), horPanel);

		model.add(this);
		initWidget(vertPanel);
	}

	@Override
	public void updateView(HeliostatState state) {
		error.getElement().getStyle().setColor("red");

		MotorState motorState = state.motorState[id.ordinal()];
		if (!pos.focused)
			pos.text.setText("" + motorState.pos);

		error.setVisible(motorState.error != null);
		error.setText(motorState.error);

		running.setText(motorState.running ? "Running" : "Stopped");

	}

}

class Button1 extends Button {
	public Button1(String string, int width, ClickHandler clickHandler) {
		super(string, clickHandler);
		setWidth(width + "px");
	}
}