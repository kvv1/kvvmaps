package kvv.heliostat.server.controller.adu;

import java.io.IOException;

public interface IPacketTransceiver {
	byte[] sendPacket(byte[] data, boolean waitResponse) throws IOException;
	void close();
}
