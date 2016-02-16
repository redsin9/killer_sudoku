package dev.ken.red.model.undo;

import java.util.Set;

import dev.ken.red.model.Cell;

/**
 * This is snapshot of Cell object with very small footprint.
 * @author kenguyen
 *
 */
public class CellSnapshot {
	public final byte row;
	public final byte col;
	public final byte value;
	public final byte[] hints;
	
	private boolean isMain;	// this cell change affects value
	
	public CellSnapshot(Cell cell, boolean isMain) {
		row = (byte) cell.row;
		col = (byte) cell.col;
		value = (byte) cell.getValue();
		
		// copy all hints
		Set<Integer> hintSet = cell.getHints();
		hints = new byte[hintSet.size()];
		int i = 0;
		for (int hint : hintSet) {
			hints[i] = (byte) hint;
			i++;
		}
		
		this.isMain = isMain;
	}
	
	public boolean isMain() {
		return isMain;
	}
	
	public void toogleMain(boolean isMain) {
		this.isMain = isMain;
	}
}
