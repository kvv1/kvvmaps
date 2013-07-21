package kvv.controllers.server;

import java.io.IOException;

import kvv.controllers.controller.Controller;

public class Test {
	public static void main(String[] args) throws IOException {
		String url = "http://localhost/rs485";

		// byte[] res = Controller.send(url, 20, new byte[] { (byte)
		// Command.CMD_GETREGS.ordinal() });

		// byte[] res = Controller.send(url, 16, new byte[] { 5, 0, 1, (byte)
		// 0x00, 0, });

		Controller controller = new Controller(url);
		
		byte[] res = controller.send(16, new byte[] { 3, 0, (byte) 0x30, 0, 1});

		for (byte b : res)
			System.out.printf("%02X ", b);

	}
}
