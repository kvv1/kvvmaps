package kvv.controllers.controller.adu;

public class PDU {
	public byte[] data;

	public PDU(byte[] data) {
		this.data = data;
	}

	public byte[] toBytes() {
		return data;
	}
}
