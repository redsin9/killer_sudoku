package dev.ken.red.dlx;

/**
 * 
 * @author kenguyen
 *
 */
class GroupHead extends Head {
	
	private final int size;
	private final int sum;
	
	private int currentSize = 0;
	private int currentSum = 0;
	
	protected GroupHead(int size, int sum) {
		this.size = size;
		this.sum = sum;
	}
	
	protected boolean addValue(int value) {
		currentSize++;
		currentSum += value;
		
		if (currentSum > sum || (currentSize == size && currentSum != sum)) {
			return false;
		}
		
		return true;
	}
	
	protected void subValue(int value) {
		currentSize--;
		currentSum -= value;
	}
	
	@Override
	protected byte getCount() {
		return Byte.MAX_VALUE;
	}
}
