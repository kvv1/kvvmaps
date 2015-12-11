package kvv.controllers.controller.adu;

import java.io.IOException;
import java.util.List;

public interface IPacketTransceiver {
	byte[] sendPacket(byte[] data, boolean waitResponse, int packetTimeout) throws IOException;
	void close();
	
	public static class PacketTimeoutException extends IOException {
		private static final long serialVersionUID = 1L;

		public PacketTimeoutException() {
			super("PACKET_TIMEOUT");
		}
	}
	
	
	
}
