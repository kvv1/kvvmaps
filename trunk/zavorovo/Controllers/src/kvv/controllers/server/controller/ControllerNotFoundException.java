package kvv.controllers.server.controller;

import java.io.IOException;

@SuppressWarnings("serial")
public class ControllerNotFoundException extends IOException {
	public ControllerNotFoundException(int addr) {
		super("Контроллер с адресом " + addr + " не определен");
	}
	public ControllerNotFoundException(String name) {
		super("Контроллер с именем " + name + " не определен");
	}
}
