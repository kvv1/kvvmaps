package kvv.goniometer.hw.sensor;

public class Sensor1 extends Transceiver{

	
	public static void main(String[] args) throws Exception {
		
		Sensor1 sensor1 = new Sensor1();
		sensor1.init("COM14", 9600, 8, 1, 0, 0);

		Thread.sleep(1000);
		sensor1.send(new byte[] {1});
		Thread.sleep(1000);
		sensor1.send(new byte[] {2, 0});
		
		
		Thread.sleep(1000);
		sensor1.close();
	}

	@Override
	protected void received(byte[] data) {
		for(byte b : data)
			System.out.print(b + " ");
		System.out.println();
	}
}
