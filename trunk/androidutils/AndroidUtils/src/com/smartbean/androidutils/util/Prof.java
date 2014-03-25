package com.smartbean.androidutils.util;

public class Prof {
	private long sum;
	private long t;
	private String name;

	public Prof(String name) {
		this.name = name;
	}

	public void print() {
		System.err.println(name + " : " + sum);
	}

	public void start() {
		t = System.currentTimeMillis();
	}

	public void stop() {
		sum += System.currentTimeMillis() - t;
	}

	public void reset() {
		sum = 0;
	}
}

