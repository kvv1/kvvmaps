package kvv.heliostat.server.controller;

import java.io.IOException;

public class NoResponseException extends IOException {
	private static final long serialVersionUID = 1L;

	public NoResponseException(String string) {
		super(string);
	}
}