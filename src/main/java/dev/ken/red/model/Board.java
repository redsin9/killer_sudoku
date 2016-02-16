package dev.ken.red.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ken.red.model.undo.CellSnapshot;
import dev.ken.red.model.undo.Step;

/**
 * 
 * @author Ken
 *
 */
public class Board {
	
private static final Logger logger = LoggerFactory.getLogger(Board.class);
	
	public static final int SIZE = Block.COUNT * Block.SIZE;
	public static final int SUM = Block.COUNT * Block.COUNT * Block.SUM;
	public static final int MIN_INDEX = 0;
	public static final int MAX_INDEX = SIZE - 1;
	
	private final Cell[][] grid = new Cell[SIZE][SIZE];
	private final Block[][] blocks = new Block[Block.COUNT][Block.COUNT];
	private Map<Integer, Group> groupMap;
	private Cell current;
	
	private int validCellCount = 0;
	
	
	
	public Board() {
		groupMap = new HashMap<Integer, Group>();
		
		logger.info("Instantiating blocks");
		for (int blockRow = 0; blockRow < Block.COUNT; blockRow++) {
			for (int blockCol = 0; blockCol < Block.COUNT; blockCol++) {
				blocks[blockRow][blockCol] = new Block(blockRow, blockCol);
			}
		}
		
		logger.info("Instantiating cells");
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				Cell cell = new Cell(row, col);
				grid[row][col] = cell;
				
				// add cell to corresponding block
				int blockRow = row / 3;
				int blockCol = col / 3;
				blocks[blockRow][blockCol].addCell(cell);
			}
		}
		
		int center = SIZE / 2;
		current = grid[center][center];
	}
	
	
	
	public Cell getCell(int row, int col) {
		if (row < MIN_INDEX || row > MAX_INDEX || col < MIN_INDEX || col > MAX_INDEX) {
			return null;
		}
		
		return grid[row][col];
	}
	
	public Collection<Group> getGroups() {
		return groupMap.values();
	}
	
	public Group getGroup(int groupId) {
		return groupMap.get(groupId);
	}
	
	public void addGroup(Group group) {
		groupMap.put(group.id, group);
	}
	
	public void subGroup(Group group) {
		groupMap.remove(group.id);
	}
	
	public Cell getCurrentCell() {
		return current;
	}
	
	public void setCurrentCell(Cell cell) {
		current = cell;
	}
	
	public void setCurrentCell(int row, int col) {
		current = grid[row][col];
	}
	
	private static final int CELL_COUNT = SIZE * SIZE;
	public boolean isSolved() {
		return validCellCount == CELL_COUNT;
	}
	
	
	
	public Set<Cell> findRelativeCellsOf(Cell cell) {
		Set<Cell> relativeCells = new HashSet<Cell>();
		
		// relative cells in the same row
		for (int otherCol = 0; otherCol < SIZE; otherCol++) {
			relativeCells.add(grid[cell.row][otherCol]);
		}
		
		// relative cells in the same col
		for (int otherRow = 0; otherRow < SIZE; otherRow++) {
			relativeCells.add(grid[otherRow][cell.col]);
		}
		
		// relative cells in the same block
		int blockRow = cell.row / 3;
		int blockCol = cell.col / 3;
		Group block = blocks[blockRow][blockCol];
		relativeCells.addAll(block.getCells());
		
		// relative cells in the same group
		Group group = groupMap.get(cell.getGroupId());
		relativeCells.addAll(group.getCells());
		
		// make sure to not include this cell in the list itself
		relativeCells.remove(cell);
		
		return relativeCells;
	}
	
	private void changeCellValue(Cell cell, int value) {
		Group group = groupMap.get(cell.getGroupId());
		group.count += value == Cell.EMPTY_VALUE ? -1 : 1;
		group.total += value - cell.getValue();
		
		cell.setValue(value);
	}
	
	
	
	//================================================================================================
	// APIs for data manipulation, START
	//================================================================================================
	public Step removeCellValue(int row, int col) {
		Cell cell = grid[row][col];
		
		// undo feature
		Step step = new Step();
		step.snap(cell, true);
		
		// if cell is valid before removing value, valid cell count is reduced
		if (cell.isValid()) {
			validCellCount--;
		}
		
		// clear cell value
		changeCellValue(cell, Cell.EMPTY_VALUE);
		
		// validate other cells
		Iterator<Cell> iterator = cell.getErrorCells().iterator();
		while (iterator.hasNext()) {
			Cell errorCell = iterator.next();
			errorCell.getErrorCells().remove(cell);
			if (errorCell.isValid()) {
				step.snap(errorCell, false);
				validCellCount++;
			}
			iterator.remove();
		}
		
		return step;
	}
	
	public Step updateCellValue(int row, int col, int value, boolean isAuto) {
		Cell cell = grid[row][col];
		
		// firstly, remove cell value (if cell is not empty) and detect affected cells
		Step step;
		if (cell.isEmpty()) {
			step = new Step();
			step.snap(cell, true);
		}
		else {
			step = removeCellValue(row, col);
		}
		
		// then set new cell value and validate
		changeCellValue(cell, value);
		for (Cell relativeCell : findRelativeCellsOf(cell)) {
			
			// error linking mechanism
			if (cell.getValue() == relativeCell.getValue() && cell.linkError(relativeCell)) {
				step.snap(relativeCell, false);
				validCellCount--;
			}
			
			// auto mode (automatically erase hints)
			if (isAuto && relativeCell.getHints().contains(value)) {
				step.snap(relativeCell, false);
				relativeCell.subHint(value);
			}
		}
		
		// detect game resolved
		if (cell.isValid()) {
			validCellCount++;
		}
		
		return step;
	}
	
	public Step addCellHint(int row, int col, int hint) {
		Cell cell = grid[row][col];
		
		Step step = new Step();
		step.snap(cell, false);
		
		cell.addHint(hint);
		
		return step;
	}
	
	public Step subCellHint(int row, int col, int hint) {
		Cell cell = grid[row][col];
		
		Step step = new Step();
		step.snap(cell, false);
		
		cell.subHint(hint);
		
		return step;
	}
	
	public Step removeCellHints(int row, int col) {
		Cell cell = grid[row][col];
		
		Step step = new Step();
		step.snap(cell, false);
		
		cell.clearHints();
		
		return step;
	}
	
	public Step auto() {
		
		// scan all candidates in the board and snap entire board
		CellSnapshot[][] gridSnapshot = new CellSnapshot[SIZE][SIZE];
		boolean[][] affectedMap = new boolean[SIZE][SIZE];
		Stack<Cell> candidates = new Stack<Cell>();
		forEach(cell -> {
			gridSnapshot[cell.row][cell.col] = new CellSnapshot(cell, false);
			
			if (cell.getHints().size() == 1) {
				candidates.push(cell);
			}
		});
		
		// no undo if there is no candidates
		if (candidates.isEmpty()) {
			return null;
		}
		
		// solve all candidates in the stack
		while (candidates.isEmpty() == false) {
			Cell candidate = candidates.pop();
			Set<Integer> hints = candidate.getHints();
			if (hints.isEmpty()) {
				// there is a chance, other candidate removed hint of this candidate
				// this means user solved the game wrong, stop auto at once
				break;
			}
			Step step = updateCellValue(candidate.row, candidate.col, hints.iterator().next(), true);
			
			// scan all affected cells, put more candidates to the stack
			for (CellSnapshot snapshot : step.snapshots) {
				affectedMap[snapshot.row][snapshot.col] = true;
				Cell cell = grid[snapshot.row][snapshot.col];
				if (cell.getHints().size() == 1) {
					candidates.push(cell);
				}
			}
		}
		
		// scan affected cells
		Step step = new Step();
		forEach(cell -> {
			int row = cell.row;
			int col = cell.col;
			if (affectedMap[row][col]) {
				CellSnapshot snapshot = gridSnapshot[row][col];
				if (snapshot.value != grid[row][col].getValue()) {
					// if there is change to cell value, mark this snapshot as main
					snapshot.toogleMain(true);
				}
				step.snapshots.add(snapshot);
			}
		});
		
		return step;
	}
	
	/**
	 * UNDO feature
	 * @param step
	 * @return
	 * the group ID of main UNDO cell
	 */
	public Set<Integer> undo(Step step) {
		List<CellSnapshot> snapshots = step.snapshots;
		
		Set<Integer> groupIds = new HashSet<Integer>();
		for (CellSnapshot snapshot : snapshots) {
			int row = snapshot.row;
			int col = snapshot.col;
			Cell cell = grid[row][col];
			
			// restore value
			if (snapshot.isMain()) {
				groupIds.add(cell.getGroupId());
				
				if (snapshot.value == Cell.EMPTY_VALUE) {
					removeCellValue(row, col);
				}
				else {
					updateCellValue(row, col, snapshot.value, false);
				}
			}
			
			// restore hints
			cell.clearHints();
			for (int hint : snapshot.hints) {
				cell.addHint(hint);
			}
		}
		
		return groupIds;
	}
	//================================================================================================
	// APIs for data manipulation, END
	//================================================================================================
	
	
	
	public void calculateAllHints() {
		forEach(cell -> {
			
			// if cell is already have value, it won't contain hints
			if (cell.isEmpty() == false) {
				return;
			}
			
			// only get possible combination of the group
			Group group = groupMap.get(cell.getGroupId());
			for (Combination combination : group.getCombinations()) {
				cell.addHints(combination.values);
			}
			
			// exclude value of non-empty relative cells
			for (Cell relativeCell : findRelativeCellsOf(cell)) {
				cell.subHint(relativeCell.getValue());
			}
		});
	}
	
	
	
	private void loadFrom(BufferedReader reader) throws IOException {
		validCellCount = 0;
		
		// parse group ID & sum
		String idLine = reader.readLine();
		String sumLine = reader.readLine();
		String[] ids = idLine.split(" ");
		String[] sums = sumLine.split(" ");
		
		// instantiate new group map
		groupMap = new HashMap<Integer, Group>(ids.length);
		for (int i = 0; i < ids.length; i++) {
			int id = Integer.parseInt(ids[i]);
			int sum = Integer.parseInt(sums[i]);
			
			Group group = new Group(id);
			group.setSum(sum);
			groupMap.put(id, group);
		}
		
		// parse cells, determine which cell belongs to which group
		reader.readLine();	// consume the blank line first
		for (int row = 0; row < SIZE; row++) {
			String line = reader.readLine();
			ids = line.split(" ");
			for (int col = 0; col < SIZE; col++) {
				int id = Integer.parseInt(ids[col]);
				
				Cell cell = grid[row][col];
				cell.setGroupId(id);
				Group group = groupMap.get(id);
				group.addCell(cell);
			}
		}
		
		// parse cell details from line with format {row},{col}:{value}|{hints}
		reader.readLine();
		String line = reader.readLine();
		while (line != null) {
			String[] buffer = line.split(":");
			String part1 = buffer[0];
			String part2 = buffer[1];
			
			// parse row,col
			buffer = part1.split(",");
			int row = Integer.parseInt(buffer[0]);
			int col = Integer.parseInt(buffer[1]);
			Cell cell = grid[row][col];
			
			// parse value and hints
			buffer = part2.split(";");
			int value = Integer.parseInt(buffer[0]);
			if (value == Cell.EMPTY_VALUE) {
				String hints = buffer.length > 1 ? buffer[1] : "";
				for (int i = 0; i < hints.length(); i++) {
					int hint = Character.digit(hints.charAt(i), 10);
					cell.addHint(hint);
				}
			}
			else {
				updateCellValue(row, col, value, false);
			}
			
			line = reader.readLine();
		}
		
		// calculate all group combinations
		for (Group group : groupMap.values()) {
			group.calculateCombinations();
		}
	}
	
	public void loadFrom(InputStream is) throws IOException {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr);
		loadFrom(reader);
		reader.close();
	}
	
	public void loadFrom(File file) throws IOException {
		FileReader fr = new FileReader(file);
		BufferedReader reader = new BufferedReader(fr);
		loadFrom(reader);
		reader.close();
	}
	
	public void saveTo(File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		BufferedWriter writer = new BufferedWriter(fw);
		
		// write id & sum lines
		StringBuilder idLine = new StringBuilder();
		StringBuilder sumLine = new StringBuilder();
		for (Group group : groupMap.values()) {
			idLine.append(String.format("%02d", group.id)).append(" ");
			sumLine.append(String.format("%02d", group.getSum())).append(" ");
		}
		idLine.append("\n");
		sumLine.append("\n\n");
		writer.write(idLine.toString());
		writer.write(sumLine.toString());
		
		// write board group map and cell details
		StringBuilder groupString = new StringBuilder();
		StringBuilder valueString = new StringBuilder();
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				Cell cell = grid[row][col];
				int groupId = cell.getGroupId();
				String data = groupId == Group.NON_ID ? "__" : String.format("%02d", groupId);
				groupString.append(data).append(" ");
				
				// write cell value (0 for empty cell)
				valueString.append(String.format("%d,%d:%d;", row, col, cell.getValue()));
				// write hints (if exists)
				for (int hint : cell.getHints()) {
					valueString.append(hint);
				}
				valueString.append("\n");
			}
			groupString.append("\n");
		}
		groupString.append("\n");
		writer.write(groupString.toString());
		writer.write(valueString.toString());
		
		writer.close();
	}

	
	
	protected static int hash(int row, int col) {
		return row * SIZE + col;
	}
	
	
	
	// new iteration method, START
	public interface Action {
		public void act(Cell cell);
	}
	
	public void forEach(Action action) {
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				action.act(grid[row][col]);
			}
		}
	}
	// new iteration method, END
}
