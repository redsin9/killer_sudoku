package dev.ken.red.model;

/**
 * 
 * @author Ken
 *
 */
public class Combination {
	public final int[] values;
	private boolean isOn;
	
	public Combination(int[] values, boolean isOn) {
		this.values = values;
		this.isOn = isOn;
	}
	
	public boolean isOn() {
		return isOn;
	}
	
	public void toggle(boolean isOn) {
		this.isOn = isOn;
	}
}
