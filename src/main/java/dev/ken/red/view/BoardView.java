package dev.ken.red.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.ken.red.model.Board;
import dev.ken.red.model.Cell;
import dev.ken.red.model.Group;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * 
 * @author Ken
 *
 */
public class BoardView extends GridPane {
	
	private static final int CELL_SPACE = 1;
	private static final int BLOCK_SPACE = 2;
	
	private static final Background BG_GRID = BackgroundFactory.create(Color.rgb(0, 0, 0));
	
	private CellView[][] grid;
	private CellView current;
	private Canvas cursor;
	
	// view logic - track all cells with same value
	private Map<String, Set<CellView>> map;
	
	
	
	public BoardView(final int SIZE) {
		this(SIZE, 
			Color.rgb(240, 240, 170), 
			Color.rgb(240, 240, 240));
	}
	
	public BoardView(final int SIZE, Color blockColor1, Color blockColor2) {
		super.setFocusTraversable(true);
		super.setFocused(true);
		super.setBackground(BG_GRID);
		super.setPadding(new Insets(BLOCK_SPACE));
		super.setVgap(BLOCK_SPACE);
		super.setHgap(BLOCK_SPACE);
		super.setAlignment(Pos.CENTER);
		
		grid = new CellView[SIZE * SIZE][SIZE * SIZE];
		
		// create SIZE x SIZE blocks
		for (int blockRow = 0; blockRow < SIZE; blockRow++) {
			for (int blockCol = 0; blockCol < SIZE; blockCol++) {
				GridPane block = new GridPane();
				block.setVgap(CELL_SPACE);
				block.setHgap(CELL_SPACE);
				block.setAlignment(Pos.CENTER);
				
				Color cellColor = blockRow % 2 == blockCol % 2 ? blockColor1 : blockColor2;
				
				// create SIZE x SIZE cells
				for (int cellRow = 0; cellRow < SIZE; cellRow++) {
					for (int cellCol = 0; cellCol < SIZE; cellCol++) {
						int gridRow = blockRow * SIZE + cellRow;
						int gridCol = blockCol * SIZE + cellCol;
						CellView cellView = new CellView(gridRow, gridCol);
						cellView.setBackground(cellColor);
						block.add(cellView, cellCol, cellRow);
						grid[gridRow][gridCol] = cellView;
					}
				}
				
				super.add(block, blockCol, blockRow);
			}
		}
		
		// init highlight stuffs
		cursor = new Canvas(CellView.SIZE, CellView.SIZE);
		setValueCursor();
		
		// logically, when the board view is instantiated, it doesn't know about the current cell yet
		// but current can't be null, otherwise the focusOn() function will get NPE at the first call
		// so we need to create a fake current cell
		current = new CellView(-1, -1);
		current.attachItem(cursor);
		
		// init map for view logic
		map = new HashMap<String, Set<CellView>>(Board.SIZE);
		for (int value = 1; value <= Board.SIZE; value++) {
			Set<CellView> set = new HashSet<CellView>();
			map.put(String.valueOf(value), set);
		}
	}
	
	public void setValueCursor() {
		final int offset = CellView.SIZE / 2 - 2;
		final int length = 16;
		GraphicsContext gc = cursor.getGraphicsContext2D();
		gc.clearRect(0, 0, CellView.SIZE, CellView.SIZE);
		gc.setFill(Color.rgb(255, 0, 0));
		gc.fillRect(offset, 0, CellView.SIZE - offset * 2, CellView.SIZE);
		gc.fillRect(0, offset, CellView.SIZE, CellView.SIZE - offset * 2);
		gc.clearRect(length, length, CellView.SIZE - length * 2, CellView.SIZE - length * 2);
	}
	
	public void setHintsCursor() {
		final int length = 18;
		final int thickness = 5;
		GraphicsContext gc = cursor.getGraphicsContext2D();
		gc.setFill(Color.rgb(255, 0, 0));
		gc.fillRect(0, 0, CellView.SIZE, CellView.SIZE);
		gc.clearRect(thickness, thickness, CellView.SIZE - thickness * 2, CellView.SIZE - thickness * 2);
		gc.clearRect(length, 0, CellView.SIZE - length * 2, CellView.SIZE);
		gc.clearRect(0, length, CellView.SIZE, CellView.SIZE - length * 2);
	}

	public CellView getCellView(int row, int col) {
		return grid[row][col];
	}
	
	public CellView getCellView(Cell cell) {
		return getCellView(cell.row, cell.col);
	}
	
	public void drawGroup(Group group) {
		
		// draw group border
		group.getCells().forEach(cell -> getCellView(cell).drawBorder(group));
		
		// draw group sum of the top left cell
		CellView target = getCellView(group.getTopLeftCell());
		target.attachSum();
		target.drawSum(group.getSum());
	}
	
	public void wipeGroup(Group group) {
		
		// prevent NPE when detect top left cell
		if (group.getCells().isEmpty()) {
			return;
		}
		
		// wipe group border
		group.getCells().forEach(cell -> getCellView(cell).wipeBorder());
		
		// wipe group sum of the top left cell
		getCellView(group.getTopLeftCell()).detachSum();
	}
	
	public void updateGroupSum(Group group, boolean remainMode) {
		CellView target = getCellView(group.getTopLeftCell());
		
		int value = group.getSum();
		if (remainMode) {
			value = group.getSum() - group.getTotal();
		}
		target.drawSum(value);
		
		target.toggleSumColor(group.isValid());
	}
	
	public void updateCellView(Cell cell) {
		CellView cellView = getCellView(cell);
		
		// draw hints
		if (cell.isEmpty()) {
			wipeCellViewValue(cell.row, cell.col);
			cellView.wipeHints();
			for (int hint : cell.getHints()) {
				cellView.toggleHint(hint, true);
			}
		}
		//draw value
		else {
			drawCellViewValue(cell.row, cell.col, cell.getValue());
			if (cell.isValid()) {
				cellView.becomeValid();
			}
			else {
				cellView.becomeError();
			}
		}
	}
	
	// update board view entirely using data from model
	public void update(Board board, boolean remainMode) {
		// redraw all group
		for (Group group : board.getGroups()) {
			if (group.getCells().isEmpty()) {
				continue;
			}
			drawGroup(group);
			if (remainMode && group.getTotal() != 0) {
				updateGroupSum(group, true);
			}
		}
		
		// redraw all cells
		board.forEach(cell -> updateCellView(cell));
		
		// highlight the current cell
		Cell current = board.getCurrentCell();
		if (current != null) {
			focusOn(current.row, current.col);
		}
	}
	
	
	
	private void wipeCellViewValue(int row, int col) {
		CellView cellView = getCellView(row, col);
		if (cellView.hasValue()) {
			
			// turn off all highlight first
			if (cellView == current) {
				toggleCellViewHighlights(cellView.getValue(), false);
			}
			map.get(cellView.getValue()).remove(cellView);
			
			// then wipe the value
			cellView.wipeValue();
		}
	}
	
	private void drawCellViewValue(int row, int col, int value) {
		
		// draw the cell view value
		CellView cellView = getCellView(row, col);
		wipeCellViewValue(row, col);
		cellView.drawValue(value);
		
		// highlight all cell view with same value
		map.get(cellView.getValue()).add(cellView);
		if (cellView == current) {
			toggleCellViewHighlights(cellView.getValue(), true);
		}
	}
	
	public void focusOn(int row, int col) {
		current.detachItem(cursor);
		if (current.hasValue()) {
			toggleCellViewHighlights(current.getValue(), false);
		}
		
		current = grid[row][col];
		current.attachItem(cursor);
		if (current.hasValue()) {
			toggleCellViewHighlights(current.getValue(), true);
		}
	}
	
	private void toggleCellViewHighlights(String value, boolean show) {
		map.get(value).stream().forEach(cellView -> cellView.toggleHighlight(show));
	}
	
	// This API is only used by Level Generator for now
	public void toggleCursor(boolean show) {
		cursor.setVisible(show);
	}
	
	
	
	// API used when game is solved
	private Timeline timeline;
	private int index = 0;
	public void congratulate() {
		
		// stop showing cursor
		toggleCursor(false);
		
		// setup animation
		KeyFrame frame = new KeyFrame(Duration.millis(100), event -> {
			index = ++index % Board.SIZE;
			Set<CellView> set = map.get(String.valueOf(index + 1));
			CellView first = set.iterator().next();
			focusOn(first.row, first.col);
		});
		timeline = new Timeline(frame);
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}
	
	public void reset() {
		// if there is animation playing, stop it
		if (timeline != null) {
			timeline.stop();
		}
		
		// reset highlight map
		map.values().stream().forEach(set -> set.clear());
		
		// wipe everything in the board
		for (int row = 0; row < Board.SIZE; row++) {
			for (int col = 0; col < Board.SIZE; col++) {
				CellView cellView = grid[row][col];
				cellView.wipeBorder();
				cellView.detachSum();
				cellView.wipeHints();
				cellView.wipeValue();
				cellView.toggleHighlight(false);
			}
		}
		
		// display the cursor again
		toggleCursor(true);
	}
}
