package dev.ken.red.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import dev.ken.red.util.Combinator;

/**
 * 
 * @author Ken
 *
 */
public class Group {
	public static final int NON_ID = -1;
	
	public final int id;
	
	protected int count = 0;	// how many cells had value
	protected int total = 0;	// total value of cells currently
	
	// statistics
	private int min, max, avg, sum;
	
	private final Map<Integer, Cell> cellMap = new LinkedHashMap<>();	// want to keep insertion order
	private final Set<Combination> combinations = new LinkedHashSet<Combination>();
	private int smallest = Integer.MAX_VALUE;	// remember smallest hash which is the top left cell
	
	public Group(int id) {
		this.id = id;
	}
	
	public void addCell(Cell cell) {
		cell.setGroupId(id);
		cellMap.put(cell.hash, cell);
		updateStatistics();
		
		// remember smallest hash
		if (smallest > cell.hash) {
			smallest = cell.hash;
		}
	}
	
	public void subCell(Cell cell) {
		cell.setGroupId(NON_ID);
		cellMap.remove(cell.hash);
		updateStatistics();
		
		// if top left cell is removed, detect other one
		if (cellMap.isEmpty() == false && smallest == cell.hash) {
			smallest = cellMap.keySet().stream().min(Comparator.comparingInt(hash -> hash)).get();
		}
	}
	
	// [BETA] remove all cells (the oldest ones) until the group size is as expected
	public void roll(int size) {
		int delta = cellMap.size() - size;
		for (int i = 0 ; i < delta; i++) {
			Cell first = cellMap.values().iterator().next();
			subCell(first);
		}
	}
	
	public Cell getTopLeftCell() {
		return cellMap.get(smallest);
	}
	
	private void updateStatistics() {
		int size = cellMap.size();
		min = (size * size + size) / 2;
		max = (19 * size - size * size) / 2;
		avg = size * 5;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
	public int getAvg() {
		return avg;
	}
	
	public void setSum(int sum) {
		this.sum = sum;
	}
	
	public int getSum() {
		return sum;
	}

	public int getTotal() {
		return total;
	}
	
	public Collection<Cell> getCells() {
		return cellMap.values();
	}
	
	public boolean containsCellAt(int row, int col) {
		return cellMap.containsKey(Board.hash(row, col));
	}
	
	public boolean containsCell(Cell cell) {
		return cellMap.containsKey(cell.hash);
	}
	
	public boolean isValid() {
		if (count < cellMap.size() && total >= sum) {
			return false;
		}
		else if (count == cellMap.size() && total != sum) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * NOTE: this API is called on demand
	 */
	public void calculateCombinations() {
		Set<int[]> results = Combinator.calculate(cellMap.size(), sum);
		if (results.isEmpty()) {
			throw new RuntimeException("Group size [" + cellMap.size() + "] and sum [" + sum + "] are invalid.");
		}
		
		combinations.clear();
		for (int[] result : results) {
			Combination combination = new Combination(result, true);
			this.combinations.add(combination);
		}
	}
	
	public Set<Combination> getCombinations() {
		return combinations;
	}
	
}
