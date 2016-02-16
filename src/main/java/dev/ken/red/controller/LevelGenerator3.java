package dev.ken.red.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import dev.ken.red.dlx.Converter;
import dev.ken.red.dlx.Randomizer;
import dev.ken.red.model.Block;
import dev.ken.red.model.Board;
import dev.ken.red.model.Cell;
import dev.ken.red.model.Group;
import dev.ken.red.view.BoardView;
import dev.ken.red.view.CellView;

/**
 * Another version of Level Generator which mainly use mouse.
 * 
 * @author kenguyen
 *
 */
public class LevelGenerator3 implements ILevelGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(LevelGenerator3.class);
	private static final int[][] DIRECTIONS = {{-1,0},{0,1},{1,0},{0,-1}};		// U, R, D, L
	
	private BoardView boardView;
	
	private Board board = new Board();
	private Cell currentCell;	// remember current cell the mouse is on
	private Group startedGroup;		// remember the group where the mouse start dragging
	private int uniqueId = 1;
	
	
	
	public LevelGenerator3() {
		logger.debug("Welcome to Level Generator 3!");
		
		boardView = new BoardView(Block.COUNT, Color.WHITESMOKE, Color.WHITESMOKE);
		boardView.toggleCursor(false);
		
		// remember which cell the mouse is pointing to
		EventHandler<MouseEvent> onMouseEntered = event -> {
			CellView cellView = (CellView) event.getSource();
			currentCell = board.getCell(cellView.row, cellView.col);
		};
		
		// form or wipe group when mouse is pressed
		EventHandler<MouseEvent> onMousePressed = event -> {
			
			// detect group of the clicked cell
			Group group = board.getGroup(currentCell.getGroupId());
			
			// if left mouse is pressed, user starts forming group
			if (event.isPrimaryButtonDown()) {
				
				// if group doesn't exist yet, create it
				if (group == null) {
					group = new Group(uniqueId++);
					board.addGroup(group);
					
					// add the cell to the group and update the view
					group.addCell(currentCell);
					updateGroup(group);
				}
				
				// remember this group for dragging event
				startedGroup = group;
			}
			// if right mouse is pressed, wipe the cell of existing group
			else if (event.isSecondaryButtonDown() && group != null) {
				removeCellFromGroup(group, currentCell);
			}
		};
		
		// detect and start the drag gesture
		EventHandler<MouseEvent> onDragDetected = event -> {
			CellView cellView = (CellView) event.getSource();
			cellView.startFullDrag();
		};
		
		// implement form and wipe group using drag gesture
		EventHandler<MouseDragEvent> onMouseDragEntered = event -> {
			CellView cellView = (CellView) event.getSource();
			Cell cell = board.getCell(cellView.row, cellView.col);
			Group otherGroup = board.getGroup(cell.getGroupId());
			MouseButton button = event.getButton();
			
			// form group - only when the started group doesn't contain this cell yet
			if (button == MouseButton.PRIMARY && startedGroup.containsCell(cell) == false) {
				
				// if this cell is already owned by other group, remove it from other group
				if (otherGroup != null && otherGroup.id != startedGroup.id) {
					removeCellFromGroup(otherGroup, cell);
				}
				
				// finally add cell to the started group and redraw it
				boardView.wipeGroup(startedGroup);
				startedGroup.addCell(cell);
				updateGroup(startedGroup);
			}
			// wipe cell of a formed group
			else if (button == MouseButton.SECONDARY && otherGroup != null) {
				removeCellFromGroup(otherGroup, cell);
			}
		};
		
		// register mouse event listener for every cell
		board.forEach(cell -> {
			CellView cellView = boardView.getCellView(cell);
			cellView.setOnMouseEntered(onMouseEntered);
			cellView.setOnMousePressed(onMousePressed);
			cellView.setOnDragDetected(onDragDetected);
			cellView.setOnMouseDragEntered(onMouseDragEntered);
		});
		
		boardView.setOnScroll(event -> {
			// detect current group where mouse is on
			Group group = board.getGroup(currentCell.getGroupId());
			if (group != null) {
				
				// increase or decrease the sum
				int sum = group.getSum() + (event.getDeltaY() > 0 ? 1 : -1);
				if (sum >= group.getMin() && sum <= group.getMax()) {
					group.setSum(sum);
					boardView.updateGroupSum(group, false);
				}
			}
		});
		
		// hidden RANDOM feature is triggered when middle mouse clicked
		boardView.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.MIDDLE) {
				// don't need to reset data, just update the view
				Board buffer = Converter.toBoard(Randomizer.generate(Block.COUNT, Block.COUNT, board));
				buffer.forEach(cell -> boardView.updateCellView(cell));
			}
		});
	}
	
	// NOTE: when this function is called, cell must be already in the group
	private void removeCellFromGroup(Group group, Cell cell) {
		
		// first of all, remove the cell of the group
		boardView.wipeGroup(group);
		group.subCell(cell);
		
		// if the group becomes empty, remove it
		if (group.getCells().isEmpty()) {
			board.subGroup(group);
		}
		// else detect if the removed cell splits the group in half
		else {
			
			// a cell might split the group in half when it has exactly 2 direct connections
			List<Cell> connections = new ArrayList<>(DIRECTIONS.length);
			for (int[] direction : DIRECTIONS) {
				int row = cell.row + direction[0];
				int col = cell.col + direction[1];
				if (group.containsCellAt(row, col)) {
					connections.add(board.getCell(row, col));
				}
			}
			
			// check if 2 connections are connected to each other by another middle-man cell
			if (connections.size() == 2) {
				Cell c1 = connections.get(0);
				Cell c2 = connections.get(1);
				boolean isConnected = false;
				if (c1.row != c2.row && c1.col != c2.col){
					isConnected = group.containsCellAt(c1.row, c2.col);
					isConnected = group.containsCellAt(c2.row, c1.col);
				}
				
				// split group in half if those 2 connections are not connected
				if (isConnected == false) {
					Group secondGroup = new Group(uniqueId++);
					transfer(group, secondGroup, c2);
					board.addGroup(secondGroup);
					updateGroup(secondGroup);
				}
			}
			
			// redraw the main group
			updateGroup(group);
		}
	}
	
	private void transfer(Group oldGroup, Group newGroup, Cell cell) {
		
		// transfer cell from old group to new group
		oldGroup.subCell(cell);
		newGroup.addCell(cell);
		
		// scan other cells which link to the transfered cell
		for (int[] direction : DIRECTIONS) {
			int row = cell.row + direction[0];
			int col = cell.col + direction[1];
			Cell connection = board.getCell(row, col);
			if (connection != null && connection.getGroupId() == oldGroup.id) {
				transfer(oldGroup, newGroup, connection);
			}
		}
	}
	
	private void updateGroup(Group group) {
		group.setSum(group.getAvg());
		boardView.drawGroup(group);
	}

	@Override
	public Node getView() {
		return boardView;
	}

	@Override
	public void export(File file) throws IOException {
		board.saveTo(file);
	}

	@Override
	public String validate() {
		// validate total sum
		int total = 0;
		for (Group group : board.getGroups()) {
			total += group.getSum();
			
			// TODO validate orphan cell in a group
			// NOTE: if we can prevent orphan node, we don't have to validate here
		}
		
		if (total != Board.SUM) {
			return "Total sum is invalid. Expected " + Board.SUM + " but was " + total;
		}
		
		return null;
	}
}
