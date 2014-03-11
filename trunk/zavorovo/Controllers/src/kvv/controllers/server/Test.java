package kvv.controllers.server;

import java.io.IOException;

import kvv.controllers.controller.Controller;

public class Test {
	static int SPM_PAGESIZE = 4;

	static int round(int n) {
		return (n + SPM_PAGESIZE - 1) & ~(SPM_PAGESIZE - 1);
	}
	
	public static void main(String[] args) throws IOException {
		String url = "http://localhost/rs485";

		for(int i = 0; i < 10; i++)
		System.out.println(i + " " + round(i));
		
		
		
	}
}
