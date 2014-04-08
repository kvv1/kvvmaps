package kvv.controllers.server.controller;

import java.io.IOException;

@SuppressWarnings("serial")
public class RegisterNotFoundException extends IOException {
	public RegisterNotFoundException(int addr, int reg) {
		super("Регистр " + reg + " в контроллере с адресом " + addr + " не определен");
	}
}
