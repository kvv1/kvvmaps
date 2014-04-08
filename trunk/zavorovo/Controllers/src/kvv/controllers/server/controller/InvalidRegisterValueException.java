package kvv.controllers.server.controller;

import java.io.IOException;

@SuppressWarnings("serial")
public class InvalidRegisterValueException extends IOException {
	public InvalidRegisterValueException(int addr, int reg, int val) {
		super("Невалидное значение. Адрес: " + addr + " регистр: " + reg
				+ " значение: " + val);
	}

}
