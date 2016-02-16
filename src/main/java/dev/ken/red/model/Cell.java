package dev.ken.red.model;

import java.util.HashSet;
import java.util.Set;



/**
 * 
 * @author Ken
 *
 */
public class Cell {
	public static final int EMPTY_VALUE = 0;
	
	public final int row;
	public final int col;
	public final int hash;
	
	private int value = EMPTY_VALUE;
	private int groupId = 0;
	private Set<Integer> hints = new HashSet<Integer>();
	
	private Set<Cell> errorCells = new HashSet<Cell>();
	
	private boolean isLocked = false;
	

	
	public Cell(int row, int col) {
		this.row = row;
		this.col = col;
		this.hash = Board.hash(row, col);
	}
	
	public int getGroupId() {
		return groupId;
	}
	
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	/**
	 * value is 1-based, 0 means empty value
	 * @return
	 */
	public int getValue() {
		return value;
	}
	
	public Set<Integer> getHints() {
		return hints;
	}
	
	public Set<Cell> getErrorCells() {
		return errorCells;
	}
	
	public boolean isValid() {
		return errorCells.isEmpty();
	}
	
	/**
	 * Cell is empty when it doesn't have value yet
	 * @return
	 */
	public boolean isEmpty() {
		return value == EMPTY_VALUE;
	}
	
	public boolean isLocked() {
		return isLocked;
	}
	
	
	
	protected void setValue(int value) {
		this.value = value;
		hints.clear();
	}
	
	protected void addHints(int... hints) {
		for (int hint : hints) {
			this.hints.add(hint);
		}
	}
	
	protected void addHint(int hint) {
		hints.add(hint);
	}
	
	protected void subHint(int hint) {
		hints.remove(hint);
	}
	
	protected void clearHints() {
		hints.clear();
	}
	
	
	
	/**
	 * 
	 * @param cell
	 * @return
	 * true if the target cell is switched from valid to error
	 */
	protected boolean linkError(Cell cell) {
		boolean isValidBefore = cell.isValid();
		this.errorCells.add(cell);
		cell.errorCells.add(this);
		return cell.isValid() != isValidBefore;
	}
	
	/**
	 * 
	 * @param cell
	 * @return
	 * true if the target cell is switched from error to valid
	 */
	protected boolean unlinkError(Cell cell) {
		boolean isValidBefore = cell.isValid();
		this.errorCells.remove(cell);
		cell.errorCells.remove(this);
		return cell.isValid() != isValidBefore;
	}
	
	protected void lock() {
		isLocked = true;
	}
	
	protected void unlock() {
		isLocked = false;
	}
	
	
	@Override
	public String toString() {
		return String.format("(%d,%d):%d", row, col, value);
	}
}
