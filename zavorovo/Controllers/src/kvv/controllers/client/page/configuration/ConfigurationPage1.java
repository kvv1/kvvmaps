package kvv.controllers.client.page.configuration;

import java.util.HashSet;
import java.util.List;

import kvv.controllers.client.ConfigurationService;
import kvv.controllers.client.ConfigurationServiceAsync;
import kvv.controllers.client.Controllers;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.UnitDescr;
import kvv.gwtutils.client.CallbackAdapter;
import kvv.gwtutils.client.CaptPanel;
import kvv.gwtutils.client.HorPanel;
import kvv.gwtutils.client.VertPanel;
import kvv.gwtutils.client.form.PanelWithButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ConfigurationPage1 extends Composite {
	static class MsgBox extends Composite {

		private VerticalPanel vp = new VerticalPanel();

		public MsgBox() {
			initWidget(vp);
		}

		void clear() {
			vp.clear();
		}

		void add(String txt) {
			Label l = new Label(txt);
			vp.add(l);
		}
	}

	private final ConfigurationServiceAsync configurationService = GWT
			.create(ConfigurationService.class);

	public final ControllersTree controllersTree = new ControllersTree();
	public final UnitsTree unitsTree = new UnitsTree();
	private MsgBox message = new MsgBox();
	private final PanelWithButton<VertPanel> panel = new PanelWithButton<VertPanel>(
			new VertPanel(new HorPanel(new CaptPanel("Контроллеры",
					controllersTree), new CaptPanel("Страницы", unitsTree)),
					message), "Сохранить") {
		@Override
		protected void onButton() {
			ErrorLocation errLoc = new ErrorLocation();

			message.clear();
			try {

				List<ControllerDescr> controllerDescrs = controllersTree
						.get(errLoc);
				List<UnitDescr> unitDescrs = unitsTree.get(errLoc);

				HashSet<String> regNames = new HashSet<String>();
				HashSet<String> cNames = new HashSet<String>();
				for (ControllerDescr cd : controllerDescrs) {
					if (cNames.contains(cd.name))
						message.add("Контроллер " + cd.name
								+ " определен повторно");
					cNames.add(cd.name);
					for (RegisterDescr rd : cd.registers) {
						if (regNames.contains(rd.name))
							message.add("Регистр " + rd.name
									+ " определен повторно");
						regNames.add(rd.name);
					}
				}

				HashSet<String> uNames = new HashSet<String>();
				for (UnitDescr ud : unitDescrs) {
					if (uNames.contains(ud.name))
						message.add("Страница " + ud.name
								+ " определена повторно");
					uNames.add(ud.name);

					for (RegisterPresentation rp : ud.registers) {
						if (!regNames.contains(rp.name))
							message.add("Страница " + ud.name
									+ " ссылается на несуществующий регистр "
									+ rp.name);
					}
				}

				configurationService.setSystemDescr(
						controllerDescrs.toArray(new ControllerDescr[0]),
						unitDescrs.toArray(new UnitDescr[0]),
						new CallbackAdapter<Void>());
			} catch (Exception e) {
				message.add("Ошибка: " + errLoc.getLoc()
						+ "\nКонфигурация не сохранена");
			}
		}
	};

	public ConfigurationPage1() {
		controllersTree.set(Controllers.systemDescr.controllers);
		unitsTree.set(Controllers.systemDescr.units);

		initWidget(panel);
	}

}
