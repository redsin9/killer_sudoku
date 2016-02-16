package dev.ken.red.model.undo;

import java.util.ArrayList;
import java.util.List;

import dev.ken.red.model.Cell;

public class Step {
	public final List<CellSnapshot> snapshots = new ArrayList<CellSnapshot>();
	
	public void snap(Cell cell, boolean isMain) {
		CellSnapshot snapshot = new CellSnapshot(cell, isMain);
		snapshots.add(snapshot);
	}
}