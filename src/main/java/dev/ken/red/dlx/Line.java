package dev.ken.red.dlx;

/**
 * Special node which represent the whole line and know about the group this line belong to
 * 
 * @author kenguyen
 *
 */
class Line extends Node {
	private static final FakeHead FAKE_HEAD = new FakeHead();
	private GroupHead groupHead = FAKE_HEAD;
	
	protected Line(byte row, byte col, byte val) {
		super(row, col, val);
	}
	
	protected void setGroupHead(GroupHead groupHead) {
		this.groupHead = groupHead;
	}
	
	protected boolean increaseSum() {
		return groupHead.addValue(val + 1);
	}
	
	protected void decreaseSum() {
		groupHead.subValue(val + 1);
	}
	
	
	
	private static final class FakeHead extends GroupHead {

		protected FakeHead() {
			super(0, 0);
		}
		
		@Override
		protected boolean addValue(int value) {
			return true;
		}
		
		@Override
		protected void subValue(int value) {
			// do nothing
		}
	}
}
