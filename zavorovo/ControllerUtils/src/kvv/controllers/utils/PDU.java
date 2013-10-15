package kvv.controllers.utils;

public class PDU {
	public byte[] data;

	public PDU(byte[] data) {
		this.data = data;
	}

	public byte[] toBytes() {
		return data;
	}
}
