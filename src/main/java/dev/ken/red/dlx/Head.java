package dev.ken.red.dlx;

/**
 * 
 * @author kenguyen
 *
 */
class Head {
	
	// pointers - direct access
	protected Head back = this;
	protected Head next = this;
	protected Node first;
	
	// values - keep track count of candidate in the column
	private byte count = 0;
	
	protected void increaseCount() {
		count++;
	}
	
	protected void decreaseCount() {
		count--;
	}
	
	protected byte getCount() {
		return count;
	}
	
	protected void unlink() {
		back.next = next;
		next.back = back;
	}
	
	protected void relink() {
		back.next = this;
		next.back = this;
	}
	
	
	
	@Override
	public String toString() {
		return String.format("(%d) %s", count, first.toString());
	}
}
