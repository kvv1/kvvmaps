package kvv.heliostat.server.envir.controller.adu;

import java.io.IOException;

public interface IPacketTransceiver {
	byte[] sendPacket(byte[] data, boolean waitResponse) throws IOException;
	void close();

	public static class PacketTimeoutException extends IOException {
		private static final long serialVersionUID = 1L;

		public PacketTimeoutException() {
			super("PACKET_TIMEOUT");
		}
	}
}
