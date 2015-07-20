package kvv.gwtutils.client.login;

import java.util.Date;

import kvv.gwtutils.client.Callback;
import kvv.simpleutils.src.MD5;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

public class LoginPanel extends Composite {

	private final LoginServiceAsync loginService = GWT
			.create(LoginService.class);

	private final HorizontalPanel hp = new HorizontalPanel();
	private final Button loginButton = new Button("login");
	private final Button logoutButton = new Button("logout");
	private final TextBox nameBox = new TextBox();
	private final TextBox passwordBox = new PasswordTextBox();

	private final CheckBox storePwd = new CheckBox("Save password");

	private final HorizontalPanel loginPanel = new HorizontalPanel();
	private final HorizontalPanel logoutPanel = new HorizontalPanel();

	private Timer timer;

	public LoginPanel() {

		nameBox.setWidth("8em");
		passwordBox.setWidth("8em");

		loginPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		loginPanel.setSpacing(4);
		loginPanel.add(new Label("User name:"));
		loginPanel.add(nameBox);
		loginPanel.add(new Label("Password:"));
		loginPanel.add(passwordBox);
		loginPanel.add(loginButton);
		loginPanel.add(storePwd);

		logoutPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		logoutPanel.setSpacing(4);
		logoutPanel.add(logoutButton);

		hp.add(loginPanel);
		hp.add(logoutPanel);

		// loginPanel.setVisible(false);
		logoutPanel.setVisible(false);

		initWidget(hp);

		logoutButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Cookies.removeCookie("pwd");

				loginService.logout(new Callback<Void>() {
					@Override
					public void onSuccess(Void result) {
						refresh();
					}

					@Override
					public void onFailure(Throwable caught) {
						refresh();
					}
				});
			}
		});

		loginButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final String name = nameBox.getText();

				final String pwdHash;
				if (Cookies.getCookie("pwd") != null)
					pwdHash = Cookies.getCookie("pwd");
				else
					pwdHash = MD5.calcMD5(name + passwordBox.getText());

				passwordBox.setText("");

				Cookies.removeCookie("pwd");

				loginService.getSessionId(new AsyncCallback<String>() {
					@Override
					public void onSuccess(String sessionId) {
						loginService.login(name,
								MD5.calcMD5(pwdHash + sessionId + "salt"),
								new Callback<Boolean>() {
									@Override
									public void onSuccess(Boolean result) {
										Date exp = new Date(System
												.currentTimeMillis()
												+ (30L * 24 * 60 * 60 * 1000));
										Cookies.setCookie("storePwd", storePwd
												.getValue() ? "true" : "false",
												exp);
										Cookies.setCookie("user", name, exp);
										if (storePwd.getValue())
											Cookies.setCookie("pwd", pwdHash,
													exp);
										refresh();
									}

									@Override
									public void onFailure(Throwable caught) {
										refresh();
										Window.alert(caught.getMessage());
									}
								});
					}

					@Override
					public void onFailure(Throwable caught) {
						refresh();
					}
				});
			}
		});

		refresh();

		timer = new Timer() {
			@Override
			public void run() {
				loginService.getSessionId(new Callback<String>());
			}
		};

		timer.scheduleRepeating(10000);
	}

	private void refresh() {
		loginService.getUser(new Callback<String>() {
			@Override
			public void onSuccess(String result) {
				if (result == null) {
					loginPanel.setVisible(true);
					logoutPanel.setVisible(false);
					passwordBox.setText("");
					storePwd.setValue("true".equals(Cookies
							.getCookie("storePwd")) ? true : false);

					String name = Cookies.getCookie("user");
					if (name != null) {
						nameBox.setText(name);
						String pwdHash = Cookies.getCookie("pwd");
						if (pwdHash != null)
							passwordBox.setText("****");
					}
				} else {
					loginPanel.setVisible(false);
					logoutPanel.setVisible(true);
					logoutButton.setText("logout " + result);
				}
			}
		});
	}

}
