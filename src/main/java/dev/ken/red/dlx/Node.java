package dev.ken.red.dlx;

/**
 * 
 * @author kenguyen
 *
 */
class Node {
	// pointers
	protected Node U = this;
	protected Node R = this;
	protected Node D = this;
	protected Node L = this;
	protected Head head;	// direct access to head of this column
	
	// values
	public final byte row;
	public final byte col;
	public final byte val;
	
	
	
	protected Node(byte row, byte col, byte val) {
		this.row = row;
		this.col = col;
		this.val = val;
	}
	
	protected byte unlink() {
		U.D = D;
		D.U = U;
		
		// if target is the first node of the head, need to re-link
		if (this == head.first) {
			head.first = D;
		}
		
		// reduce head count and determine if the candidate is good or bad
		head.decreaseCount();
		return head.getCount();
	}
	
	protected void relink() {
		U.D = this;
		D.U = this;
		
		// if below is first node of head, re-link
		if (head.first == D) {
			head.first = this;
		}
		
		// increase head count
		head.increaseCount();
	}
	
	/**
	 * get the heap address of the object for troubleshooting purpose
	 * @return
	 */
	public String address() {
		return Integer.toHexString(super.hashCode());
	}
	
	@Override
	public String toString() {
		return String.format("%d,%d:%d@%d", row, col, val, address());
	}
	
	
	
	protected static Node mock() {
		byte mock = 0;
		return new Node(mock, mock, mock);
	}
}
