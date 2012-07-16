package kvv.controllers.server.rs485;

import java.io.IOException;

public interface Transceiver {
	byte[] send(int addr, byte[] data) throws IOException;
	void close();
}
