package dev.ken.red.util;

public class Counter {
	private final int max;
	private int counter;
	
	private String name = "";
	
	public Counter(int max, String name) {
		this.max = max;
		this.name = name;
	}
	
	public void count() {
		counter++;
		if (counter > max) {
			throw new RuntimeException("[" + name + "] exceeded " + max);
		}
	}
}
