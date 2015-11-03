package kvv.controllers.controller.adu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ModbusPacketTransceiver {
	private static final long BYTE_TIMEOUT = 50;

	protected byte[] sendPacket(byte[] data, InputStream inputStream,
			OutputStream outputStream, boolean waitResponse, int packetTimeout)
			throws IOException {

		while (inputStream.available() > 0)
			inputStream.read();
		outputStream.write(data);
		outputStream.flush();

		if (!waitResponse)
			return null;

		long timeout = packetTimeout;

		List<Byte> buffer = new ArrayList<>();

		long t = System.currentTimeMillis();

		for (;;) {
			while (System.currentTimeMillis() - t < timeout
					&& inputStream.available() == 0) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {
				}
			}
			if (inputStream.available() == 0) {
				if (buffer.isEmpty())
					throw new IPacketTransceiver.PacketTimeoutException();
				byte[] res = new byte[buffer.size()];
				for (int i = 0; i < buffer.size(); i++) {
					res[i] = buffer.get(i);
				}
				return res;
			} else {
				while (inputStream.available() > 0)
					buffer.add((byte) inputStream.read());
				timeout = BYTE_TIMEOUT;
				t = System.currentTimeMillis();
			}
		}
	}
}
