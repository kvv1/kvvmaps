package kvv.controllers.server.controller;

import java.io.IOException;

@SuppressWarnings("serial")
public class ControllerTypeNotFoundException extends IOException {
	public ControllerTypeNotFoundException(String type) {
		super("controller type " + type + " not defined");
	}
}
