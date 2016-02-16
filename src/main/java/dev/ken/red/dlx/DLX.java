package dev.ken.red.dlx;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Stack;

import dev.ken.red.model.Board;
import dev.ken.red.model.Cell;
import dev.ken.red.model.Group;

/**
 * non-public DLX engine
 * 
 * @author kenguyen
 *
 */
class DLX {
	public static final int CON_COUNT = 4;
	
	protected Head main = new Head();
	
	private Head[] heads;			// horizontal indexes (not used so far)
	private Line[] lines;		// vertical indexes
	private Stack<Node> steps = new Stack<Node>();
	
	// remember board size for quick accessing head later
	private final int BOARD_SIZE;
	private final int CON_SIZE;
	
	protected DLX(int blockCount, int blockSize) {
		BOARD_SIZE = blockCount * blockSize;
		if (BOARD_SIZE > 100) {
			// maximum allowed size is 100x100
			throw new RuntimeException("Board size is too large.");
		}
		
		CON_SIZE = BOARD_SIZE * BOARD_SIZE;
		final int headCount = CON_SIZE * CON_COUNT;
		
		// build the main head list
		heads = new Head[headCount];
		for (short i = 0; i < headCount; i++) {
			
			// instantiate head and also remember index
			Head head = new Head();
			
			// create fake node to assign to first node of head
			head.first = Node.mock();
			heads[i] = head;
			
			// insert this head to the end of the main head list
			Head last = main.back;
			last.next = head;
			head.back = last;
			head.next = main;
			main.back = head;
		}
		
		// index all lines
		lines = new Line[BOARD_SIZE * CON_SIZE];
		
		// traverse through all candidates and build dancing links matrix
		for (byte row = 0; row < BOARD_SIZE; row++) {
			for (byte col = 0; col < BOARD_SIZE; col++) {
				for (byte val = 0; val < BOARD_SIZE; val++) {
					
					// create 4 nodes for 4 constraints and link them together
					Node[] nodes = new Node[CON_COUNT];
					
					// create and remember the first node
					Line line = new Line(row, col, val);
					nodes[0] = line;
					lines[toLineIndex(row, col, val)] = line;
					
					// create the other nodes and link them together horizontally
					for (int i = 1; i < CON_COUNT; i++) {
						nodes[i] = new Node(row, col, val);
						nodes[i].L = nodes[i - 1];
						nodes[i - 1].R = nodes[i];
					}
					nodes[0].L = nodes[CON_COUNT - 1];
					nodes[CON_COUNT - 1].R = nodes[0];
					
					// find correct constraint column for each node
					for (int i = 0; i < CON_COUNT; i++) {
						Node node = nodes[i];
						
						// calculate head index
						int headIndex = CON_SIZE * i;
						switch(i) {
						// constraint 1 - each node has exactly one value
						case 0:
							headIndex += row * BOARD_SIZE + col;
							break;
							
						// constraint 2 - each row must have unique nodes
						case 1:
							headIndex += row * BOARD_SIZE + val;
							break;
							
						// constraint 3 - each column must have unique nodes
						case 2:
							headIndex += col * BOARD_SIZE + val;
							break;
							
						// constraint 4 - each block must have unique nodes
						case 3:
							int blockRow = row / blockSize;
							int blockCol = col / blockSize;
							int blockIndex = blockRow * blockCount + blockCol;
							headIndex += blockIndex * blockSize * blockSize + val;
							break;
						}
						
						// append this new node to the column
						Head head = heads[headIndex];
						node.head = head;
						Node first = head.first;
						Node last = first.U;
						last.D = node;
						node.U = last;
						node.D = first;
						first.U = node;
						head.increaseCount();
					}
				}
			}
		}
		
		// disconnect the fake node out of every column
		for (int i = 0; i < headCount; i++) {
			Head head = heads[i];
			Node fake = head.first;
			Node first = fake.D;
			Node last = fake.U;
			
			// first and last nodes now don't have fake node in between anymore
			first.U = last;
			last.D = first;
			
			// head now points to the real first node
			head.first = first;
		}
	}
	
	
	
	// TODO under development
	private void appendGroupConstraints(Group group) {
		// group constraints is special, they won't have head
		// they are only nodes used to linked other 4 domains of constraints
		// each group has 9 columns representing 9 possible values
		// each column has N nodes where N is the number of cells in the group
		
		// special head which can handle sum logic
		GroupHead groupHead = new GroupHead(group.getCells().size(), group.getSum());
		
		// create 9 columns of nodes for 9 possible values
		for (byte val = 0 ; val < BOARD_SIZE; val++) {
			
			// use a buffer node to link all node of same column together
			Node buffer = Node.mock();
			
			for (Cell cell : group.getCells()) {
				byte row = (byte) cell.row;
				byte col = (byte) cell.col;
				Node node = new Node(row, col, val);
				node.head = groupHead;
				
				// hook the node to the correct line
				Line line = lineAt(row, col, val);
				line.setGroupHead(groupHead);
				Node last = line.L;
				last.R = node;
				node.L = last;
				node.R = line;
				line.L = node;
				
				// hook all nodes of same column together - keep appending new node to the bottom
				Node bottom = buffer.U;
				bottom.D = node;
				node.U = bottom;
				
				// then the node becomes bottom node
				buffer.U = node;
			}
			
			// at the end, link top and bottom nodes together, skip the buffer node
			Node top = buffer.D;
			Node bottom = buffer.U;
			bottom.D = top;
			top.U = bottom;
		}
	}
	
	
	
	protected void applyBoardConstraints(Board board) {
		final boolean[] VAL_MASK = new boolean[BOARD_SIZE];
		board.getGroups().stream().forEach(group -> {
			Collection<Cell> cells = group.getCells();
			
			// if group has only one cell, the sum of group is the value of the cell
			// so this cell can be solved right away
			if (cells.size() == 1) {
				Cell cell = cells.iterator().next();
				board.updateCellValue(cell.row, cell.col, group.getSum(), false);
				return;
			}
			
			appendGroupConstraints(group);
			
			// scan and remove impossible values
			boolean[] valMask = VAL_MASK.clone();
			group.calculateCombinations();
			group.getCombinations().stream().forEach(combination -> {
				for (int value : combination.values) {
					valMask[value - 1] = true;		// valMask is 0-based, values are 1-based
				}
			});
			
			// indexes of all elements with value false are impossible value of the group
			for (byte val = 0; val < valMask.length; val++) {
				if (valMask[val] == false) {
					// remove all rows which are impossible values
					for (Cell cell : cells) {
						Node line = lineAt(cell.row, cell.col, val);
						remove(line);
					}
				}
			}
		});
		
		// apply fixed cells (cells already had value)
		board.forEach(cell -> {
			if (cell.isEmpty() == false) {
				Node row = lineAt(cell.row, cell.col, cell.getValue() - 1);
				cover(row);
			}
		});
	}
	
	
	
	public Collection<Node> getSteps() {
		return new LinkedList<>(steps);
	}
	
	
	
	protected Boolean cover(Node candidate) {
		
		// implement group constraints check here before actually cover this row
		// if group constraints are not satisfied, return null right away
		Line line  = lineAt(candidate.row, candidate.col, candidate.val);
		if (line.increaseSum() == false) {
			line.decreaseSum();		// undo
			return null;
		}
		
		// tracking this step
		steps.push(candidate);
		
		// traverse through all columns in this candidate
		Boolean result = true;
		Node column = candidate;
		do {
			
			// unlink head of this column from head list
			column.head.unlink();
			
			// traverse other rows (except this row) which linked to this node
			for (Node otherRow = column.D; otherRow != column; otherRow = otherRow.D) {
				
				// traverse all nodes of this row, except the first one
				for (Node node = otherRow.R; node != otherRow; node = node.R) {
					
					// link above and below nodes to each other, skip this one
					// NOTE: the target node still remember about above and below node
					// so it is able to restore links when uncovering
					if (node.unlink() < 1) {
						result = false;
					}
				}
			}
			
			column = column.R;
		} while (column != candidate);
		
		// return true if there is not any head which count is less than 1
		return result;
	}
	
	
	
	protected void uncover() {

		// get the latest step
		Node candidate = steps.pop();
		
		// decrease sum of the group this candidate belongs to
		lineAt(candidate.row, candidate.col, candidate.val).decreaseSum();
		
		// traverse through all columns of this row
		// go left from the last node back to this node
		Node column = candidate.L;
		do {
			
			// re-link head to the head list
			column.head.relink();
			
			// traverse through all other rows which link to same node with this column 
			// go up from the bottom row
			for (Node otherRow = column.U; otherRow != column; otherRow = otherRow.U) {
				
				// traverse through all other nodes of this row and re-link
				// go left from the last node
				for (Node node = otherRow.L; node != otherRow; node = node.L) {
					node.relink();
				}
			}
			
			column = column.L;
		} while (column != candidate.L);
	}
	
	
	
	protected Head findSmallestHead() {
		Head smallest = main.next;
		
		for (Head next = smallest; next != main; next = next.next) {
			if (next.getCount() < smallest.getCount()) {
				smallest = next;
			}
		}
		
		return smallest;
	}
	
	
	
	private int toLineIndex(int row, int col, int val) {
		return row * CON_SIZE + col * BOARD_SIZE + val;
	}
	
	private Line lineAt(int row, int col, int val) {
		return lines[toLineIndex(row, col, val)];
	}
	
	private void remove(Node candidate) {
		// go through all node and unlink it with its above and below nodes
		Node node = candidate;
		do {
			node.unlink();
			node = node.R;
		} while (node != candidate);
	}
}
