package dev.ken.red.model;

/**
 * The board size is defined by this class
 * 
 * @author Ken
 *
 */
public class Block extends Group {
	public static final int COUNT = 3;
	public static final int SIZE = 3;
	public static final int SUM = (int) (Math.pow(SIZE, 4) + Math.pow(SIZE, 2)) / 2;
	
	public final int row;
	public final int col;

	public Block(int row, int col) {
		super(Group.NON_ID);
		this.row = row;
		this.col = col;
	}
	
	@Override
	final public int getSum() {
		return SUM;
	}
	
	@Override
	final public void setSum(int sum) {
		throw new RuntimeException("Sum of block is unchanged and always " + SUM + ".");
	}
	
	@Override
	final public void subCell(Cell cell) {
		throw new RuntimeException("Can't remove cell from a block.");
	}
}
