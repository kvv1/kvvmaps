package kvv.manchester;

import kvv.manchester.ManchesterDecoder;
import kvv.manchester.ManchesterDecoder.DecoderListener;
import kvv.manchester.ManchesterEncoder;
import kvv.manchester.Packet;

public class Comm {
	private byte[] response;
	private byte packNum;
	private Object sync = new Object();


	private final Transmitter transmitter = new Transmitter();
	private final Receiver receiver = new Receiver(new ManchesterDecoder(
			new DecoderListener() {
				@Override
				public void received(byte[] data) {
					synchronized (sync) {
						if (data[0] == packNum) {
							System.out.println("** " + packNum);
							response = new byte[data.length - 1];
							System.arraycopy(data, 1, response, 0,
									data.length - 1);
							sync.notify();
						}
					}
				}
			}));

	public synchronized byte[] request(byte[] data) {
		byte[] data1 = Packet.createPacket(packNum, data);
		response = null;

		synchronized (sync) {
			
			transmitter.send(ManchesterEncoder.encode(data1));
			try {
				sync.wait(1000);
			} catch (InterruptedException e) {
			}
		}

		packNum++;

		return response;
	}

	public void stop() {
		transmitter.stop();
		receiver.stop();
	}

	public void start() {
		transmitter.start();
		receiver.start();
	}

}
