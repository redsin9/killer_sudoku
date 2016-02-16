package dev.ken.red.controller;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import dev.ken.red.model.Block;
import dev.ken.red.model.Board;
import dev.ken.red.model.Cell;
import dev.ken.red.model.Group;
import dev.ken.red.view.BoardView;
import dev.ken.red.view.CellView;

/**
 * 
 * @author Ken
 *
 */
public class LevelGenerator2 implements 
	EventHandler<KeyEvent>, ILevelGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(LevelGenerator2.class);
	
	private static final Color UNSELECTED = Color.WHITESMOKE;
	private static final Color SELECTED = Color.LIME;
	private static final Color INVALID = Color.LIGHTPINK;
	
	private Board board;
	private BoardView boardView;
	
	private Node pointer;
	private Node head;
	private Node curr;
	private int groupId = 0;
	
	private boolean sumMode = false;
	
	public LevelGenerator2() {
		board = new Board();
		board.setCurrentCell(0, 0);
		
		boardView = new BoardView(Block.COUNT, UNSELECTED, UNSELECTED);
		boardView.focusOn(0, 0);
		boardView.setOnKeyPressed(this);
		
		head = new Node(groupId++);
		curr = head;
	}
	
	private void newNode() {
		// current node will become previous node
		Node back = curr;
		
		// instantiate new node to become current node
		curr = new Node(groupId++);
		
		// link current node and previous node
		curr.back = back;
		back.next = curr;
		
		// link current node and head to create circle linked list
		curr.next = head;
		head.back = curr;
	}
	
	
	
	@Override
	public void handle(KeyEvent event) {
		KeyCode code = event.getCode();
		if (sumMode) {
			switch(code) {
			case TAB:
				// validate group before leaving it
				try {
					pointer.calculateCombinations();
					pointer.isValid = true;
					dehighlightNode(pointer);
				}
				catch (Exception e) {
					pointer.isValid = false;
					markErrorNode(pointer);
				}
				
				// highlight next group and remove error signs
				pointer = event.isShiftDown() ? pointer.back : pointer.next;
				highlightNode(pointer);
				
				break;
				
			default:
				if (code.isDigitKey()) {
					String text = event.getText();
					int value = Integer.parseInt(text);
					handleValue(value);
				}
				break;
			}
		}
		else {
			Cell current = board.getCurrentCell();
			int row = current.row;
			int col = current.col;
			
			switch(code) {
			case A:
				if (col > 0) col--;
				break;
				
			case W:
				if (row > 0) row--;
				break;
				
			case S:
				if (row < 8) row++;
				break;
				
			case D:
				if (col < 8) col++;
				break;
				
			case SHIFT:
				CellView cellView = boardView.getCellView(current);
				if (curr.containsCell(current)) {
					curr.subCell(current);
					cellView.setBackground(UNSELECTED);
				}
				else {
					curr.addCell(current);
					cellView.setBackground(SELECTED);
				}
				break;
				
			case ENTER:
				createGroup();
				break;
				
			case DELETE:
				deleteGroup();
				break;
				
			default:
				break;
			}
			
			if (row != current.row || col != current.col) {
				board.setCurrentCell(row, col);
				boardView.focusOn(row, col);
			}
		}
		
		event.consume();
	}
	
	private void createGroup() {
		// do some basic validation first
		Collection<Cell> cells = curr.getCells();
		if (cells.isEmpty() || cells.size() > Board.SIZE) {
			return;
		}
		// TODO all cells have to touch each other
		
		// update model
		board.addGroup(curr);
		
		// update view
		boardView.drawGroup(curr);
		Cell topRightCell = board.getCell(Board.MAX_INDEX, Board.MIN_INDEX);
		for (Cell cell : cells) {
			if (topRightCell.row > cell.row) {
				topRightCell = cell;
			}
			else if (topRightCell.row == cell.row && topRightCell.col < cell.col) {
				topRightCell = cell;
			}
			boardView.getCellView(cell).setBackground(UNSELECTED);
		}
		
		// move cursor to the next position (the best position)
		Cell nextCell = null;
		for (int row = topRightCell.row; row < Board.SIZE; row++) {
			for (int col = 0; col < Board.SIZE; col++) {
				Cell cell = board.getCell(row, col);
				if (cell.getGroupId() == Group.NON_ID) {
					nextCell = cell;
					break;
				}
			}
			
			if (nextCell != null) {
				break;
			}
		}
		
		if (nextCell == null) {
			sumMode = true;
			boardView.toggleCursor(false);
			
			// set pointer to head
			pointer = head;
			highlightNode(pointer);
		}
		else {
			newNode();
			board.setCurrentCell(nextCell.row, nextCell.col);
			boardView.focusOn(nextCell.row, nextCell.col);
		}
	}
	
	private void deleteGroup() {
		// don't delete group which is not created yet
		int groupId = board.getCurrentCell().getGroupId();
		if (groupId == curr.id || groupId == Group.NON_ID) {
			return;
		}
		
		// find the group we are deleting
		for (Node pointer = curr.back; pointer.back != null; pointer = pointer.back) {
			if (pointer.id == groupId) {
				// update view
				boardView.wipeGroup(pointer);
				
				// update model
				board.subGroup(pointer);
				for (Cell cell : pointer.getCells()) {
					cell.setGroupId(Group.NON_ID);
				}
				
				// update circle linked list
				Node back = pointer.back;
				Node next = pointer.next;
				back.next = next;
				next.back = back;
				
				// if pointer is head, move head to next node
				if (pointer == head) {
					head = next;
				}
				pointer = null;
				
				break;
			}
		}
	}
	
	private void dehighlightNode(Node node) {
		for (Cell cell : node.getCells()) {
			boardView.getCellView(cell).setBackground(UNSELECTED);
		}
	}
	
	private void highlightNode(Node node) {
		for (Cell cell : node.getCells()) {
			boardView.getCellView(cell).setBackground(SELECTED);
		}
	}
	
	private void markErrorNode(Node node) {
		for (Cell cell : node.getCells()) {
			boardView.getCellView(cell).setBackground(INVALID);
		}
	}
	
	private void handleValue(int value) {
		int sum = pointer.getSum();
		// if sum has 2 digits, only keep the one on the right
		if (sum > 9) {
			sum = sum % 10;
		}
		sum = sum * 10 + value;
		pointer.setSum(sum);
		boardView.updateGroupSum(pointer, false);
	}
	
	@Override
	public javafx.scene.Node getView() {
		return boardView;
	}
	
	@Override
	public String validate() {
		logger.debug("Validating level");
		int totalSum = 0;
		
		Node node = head;
		do {
			if (node.isValid == false) {
				return "There is invalid group in the level.";
			}
			totalSum += node.getSum();
			node = node.next;
		} 
		while (node != head);
		
		if (totalSum != Board.SUM) {
			return "Total sum is invalid. Expected " + Board.SUM + " but was " + totalSum;
		}
		
		return null;
	}
	
	
	
	@Override
	public void export(File file) throws IOException {
		board.saveTo(file);
	}
	
	
	
	private class Node extends Group {
		public Node next;
		public Node back;
		public boolean isValid = false;
		
		public Node(int id) {
			super(id);
		}
	}
}
