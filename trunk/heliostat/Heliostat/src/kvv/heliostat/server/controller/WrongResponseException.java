package kvv.heliostat.server.controller;

import java.io.IOException;

public class WrongResponseException extends IOException {
	private static final long serialVersionUID = 1L;

	public WrongResponseException(String string) {
		super(string);
	}
}