package kvv.controllers.utils;

public class RTU {
	public byte[] data;

	public RTU(byte[] data) {
		this.data = data;
	}

	public byte[] toBytes() {
		return data;
	}
}
