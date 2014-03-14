package kvv.controllers.client.page;

import java.util.HashSet;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ConfigurationService;
import kvv.controllers.client.ConfigurationServiceAsync;
import kvv.controllers.client.Controllers;
import kvv.controllers.client.page.config.EditableTree;
import kvv.controllers.client.page.config.ItemFactory;
import kvv.controllers.client.page.config.TextWithLabel;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.UnitDescr;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

class MsgBox extends Composite {

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

public class ConfigurationPageG extends Composite {
	private final ConfigurationServiceAsync configurationService = GWT
			.create(ConfigurationService.class);

	private VerticalPanel vpanel = new VerticalPanel();
	private HorizontalPanel hpanel = new HorizontalPanel();
	private Button save = new Button("Сохранить");

	private MsgBox message = new MsgBox();

	static class ErrorLocation {
		ControllerDescr cd;
		RegisterDescr rd;
		UnitDescr ud;
		RegisterPresentation rp;

		public void clear() {
			cd = null;
			rd = null;
			ud = null;
			rp = null;
		}

		public String getLoc() {
			String loc = "";
			if (cd != null)
				loc += "контроллер " + cd.name;
			if (ud != null)
				loc += "страница " + ud.name;
			if (rd != null)
				loc += " регистр " + rd.name;
			if (rp != null)
				loc += " регистр " + rp.name;
			return loc;
		}
	}

	private ErrorLocation errLoc;

	public ConfigurationPageG() {

		final ControllersTree controllers = new ControllersTree();
		final UnitsTree units = new UnitsTree();

		controllers.set(Controllers.systemDescr.controllers);
		units.set(Controllers.systemDescr.units);

		// configurationService.getSystemDescr(new
		// CallbackAdapter<SystemDescr>() {
		//
		// @Override
		// public void onSuccess(SystemDescr result) {
		// controllers.set(result.controllers);
		// units.set(result.units);
		// }
		// });

		hpanel.add(controllers);
		hpanel.add(units);

		vpanel.add(hpanel);

		vpanel.add(message);

		vpanel.add(save);

		initWidget(vpanel);

		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				message.clear();
				try {
					errLoc = new ErrorLocation();
					ControllerDescr[] controllerDescrs = controllers.get();
					UnitDescr[] unitDescrs = units.get();

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
								message.add("Страница "
										+ ud.name
										+ " ссылается на несуществующий регистр "
										+ rp.name);
						}
					}

					configurationService.setSystemDescr(controllerDescrs,
							unitDescrs, new CallbackAdapter<Void>());
				} catch (Exception e) {
					message.add("Ошибка: " + errLoc.getLoc()
							+ "\nКонфигурация не созранена");
				}
			}
		});
	}

	class ControllersTree extends EditableTree {
		{
			setWidth("500px");
		}

		public void set(ControllerDescr[] controllerDescrs) {
			final ItemFactory<RegisterDescr> rFactory = new ItemFactory<RegisterDescr>() {

				@Override
				public Widget createWidget(RegisterDescr rd) {
					return new RegisterForm(rd);
				}

				@Override
				public void createInterior(TreeItem item, RegisterDescr rd) {
				}
			};

			ItemFactory<ControllerDescr> cFactory = new ItemFactory<ControllerDescr>() {

				@Override
				public Widget createWidget(ControllerDescr cd) {
					return new ControllerForm(cd);
				}

				@Override
				public void createInterior(TreeItem item, ControllerDescr cd) {
					addListItem(item, "Регистры", rFactory, cd == null ? null
							: cd.registers);
				}
			};

			TreeItem item = addListItem(null, "Контроллеры", cFactory,
					controllerDescrs);
			item.setState(true);
		}

		public ControllerDescr[] get() {
			TreeItem controllersItem = getItem(0);
			int n = controllersItem.getChildCount();
			ControllerDescr[] res = new ControllerDescr[n];
			for (int i = 0; i < n; i++) {
				errLoc.clear();
				ControllerForm form = getForm(controllersItem.getChild(i));
				ControllerDescr cd = new ControllerDescr();
				errLoc.cd = cd;
				cd.name = form.name.getText();
				cd.addr = form.addr.getNum();
				cd.type = form.type.getItemText(form.type.getSelectedIndex());

				TreeItem regsItem = controllersItem.getChild(i).getChild(0);

				int regsN = regsItem.getChildCount();

				cd.registers = new RegisterDescr[regsN];

				for (int j = 0; j < regsN; j++) {
					RegisterForm regForm = getForm(regsItem.getChild(j));
					RegisterDescr rd = new RegisterDescr();
					errLoc.rd = rd;
					rd.name = regForm.name.getText();
					rd.register = regForm.num.getNum();
					cd.registers[j] = rd;
				}

				res[i] = cd;
			}
			return res;
		}
	}

	class UnitsTree extends EditableTree {
		{
			setWidth("700px");
		}

		public void set(UnitDescr[] unitDescrs) {
			final ItemFactory<RegisterPresentation> rFactory = new ItemFactory<RegisterPresentation>() {

				@Override
				public Widget createWidget(RegisterPresentation rd) {
					return new RegisterPresentationForm(rd);
				}

				@Override
				public void createInterior(TreeItem item,
						RegisterPresentation rd) {
				}
			};

			ItemFactory<UnitDescr> uFactory = new ItemFactory<UnitDescr>() {

				@Override
				public Widget createWidget(UnitDescr ud) {
					return new UnitForm(ud);
				}

				@Override
				public void createInterior(TreeItem item, UnitDescr ud) {
					addListItem(item, "Регистры", rFactory, ud == null ? null
							: ud.registers);
				}
			};

			TreeItem item = addListItem(null, "Страницы", uFactory, unitDescrs);
			item.setState(true);
		}

		public UnitDescr[] get() {
			TreeItem unitsItem = getItem(0);
			int n = unitsItem.getChildCount();
			UnitDescr[] res = new UnitDescr[n];
			for (int i = 0; i < n; i++) {
				errLoc.clear();
				UnitForm form = getForm(unitsItem.getChild(i));
				UnitDescr ud = new UnitDescr();
				errLoc.ud = ud;
				ud.name = form.name.getText();

				TreeItem regsItem = unitsItem.getChild(i).getChild(0);

				int regsN = regsItem.getChildCount();

				ud.registers = new RegisterPresentation[regsN];

				for (int j = 0; j < regsN; j++) {
					RegisterPresentationForm regForm = getForm(regsItem
							.getChild(j));
					RegisterPresentation rp = new RegisterPresentation();
					errLoc.rp = rp;
					rp.name = regForm.name.getText();
					rp.height = regForm.heigth.getNum();
					rp.min = regForm.min.getNum();
					rp.max = regForm.max.getNum();
					rp.step = regForm.step.getNum();
					ud.registers[j] = rp;
				}

				res[i] = ud;
			}
			return res;
		}
	}

	static class ControllerForm extends Composite {
		HorizontalPanel hp = new HorizontalPanel();
		TextWithLabel name = new TextWithLabel("", 150, false);
		TextWithLabel addr = new TextWithLabel("Адрес", 30, true);
		ListBox type = new ListBox();
		{
			type.addItem("");
			for (String t : Controllers.systemDescr.controllerTypes.keySet())
				type.addItem(t);
		}

		public ControllerForm(ControllerDescr cd) {
			String name = cd == null ? "" : cd.name;
			int addr = cd == null ? 0 : cd.addr;

			if (cd != null)
				for (int i = 0; i < this.type.getItemCount(); i++)
					if (this.type.getItemText(i).equals(cd.type))
						this.type.setSelectedIndex(i);

			hp.setSpacing(2);
			hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			this.name.textBox.setText(name);
			this.addr.textBox.setText("" + addr);
			hp.add(this.name);
			hp.add(this.addr);
			hp.add(this.type);
			initWidget(hp);
		}
	}

	static class RegisterForm extends Composite {
		HorizontalPanel hp = new HorizontalPanel();
		TextWithLabel name = new TextWithLabel("", 150, false);
		TextWithLabel num = new TextWithLabel("N", 30, true);

		public RegisterForm(RegisterDescr rd) {
			String name = rd == null ? "" : rd.name;
			int num = rd == null ? 0 : rd.register;
			hp.setSpacing(2);
			hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			this.name.textBox.setText(name);
			this.num.textBox.setText("" + num);
			hp.add(this.name);
			hp.add(this.num);
			initWidget(hp);
		}
	}

	static class UnitForm extends Composite {
		HorizontalPanel hp = new HorizontalPanel();
		TextWithLabel name = new TextWithLabel("", 150, false);

		public UnitForm(UnitDescr unit) {
			hp.setSpacing(2);
			hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			this.name.textBox.setText(unit == null ? "" : unit.name);
			hp.add(this.name);
			initWidget(hp);
		}
	}

	static class RegisterPresentationForm extends Composite {
		HorizontalPanel hp = new HorizontalPanel();
		TextWithLabel name = new TextWithLabel("", 150, false);
		TextWithLabel heigth = new TextWithLabel("Высота", 30, true);
		TextWithLabel min = new TextWithLabel("Min", 30, true);
		TextWithLabel max = new TextWithLabel("Max", 30, true);
		TextWithLabel step = new TextWithLabel("Шаг", 30, true);

		String tostr(Integer i) {
			return i == null ? "" : "" + i;
		}

		public RegisterPresentationForm(RegisterPresentation rp) {

			String name = rp == null ? "" : rp.name;
			String h = rp == null ? "" : tostr(rp.height);
			String min = rp == null ? "" : tostr(rp.min);
			String max = rp == null ? "" : tostr(rp.max);
			String step = rp == null ? "" : tostr(rp.step);

			hp.setSpacing(2);

			hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			this.name.textBox.setText(name);
			hp.add(this.name);

			this.heigth.textBox.setText(h);
			hp.add(this.heigth);

			this.min.textBox.setText(min);
			hp.add(this.min);

			this.max.textBox.setText(max);
			hp.add(this.max);

			this.step.textBox.setText(step);
			hp.add(this.step);

			initWidget(hp);
		}
	}

}
