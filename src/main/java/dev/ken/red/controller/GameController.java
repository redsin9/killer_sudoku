package dev.ken.red.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import dev.ken.red.model.Block;
import dev.ken.red.model.Board;
import dev.ken.red.model.Cell;
import dev.ken.red.model.Group;
import dev.ken.red.model.undo.CellSnapshot;
import dev.ken.red.model.undo.Step;
import dev.ken.red.util.LevelUtils;
import dev.ken.red.view.AlertFactory;
import dev.ken.red.view.BackgroundFactory;
import dev.ken.red.view.BoardView;
import dev.ken.red.view.CellView;
import dev.ken.red.view.CombinatorWidget;
import dev.ken.red.view.TimerWidget;
import dev.ken.red.view.CombinationView;

/**
 * 
 * @author Ken
 *
 */
public class GameController implements EventHandler<KeyEvent> {
	private static Logger logger = LoggerFactory.getLogger(GameController.class);
	
	// model
	private Board board;
	
	// views
	public final GridPane view;
	private BoardView boardView;
	private VBox controlPanel;
	private TimerWidget timer;
	private CheckBox hintMode;
	private CheckBox autoMode;
	private CheckBox remainMode;
	private CombinationView combinationView;
	private CombinatorWidget combinator;
	
	// UNDO feature
	private Stack<Step> steps = new Stack<Step>();
	
	
	
	public GameController() {
		logger.debug("Instantiating model");
		board = new Board();
		
		logger.debug("Instantiating views");
		boardView = new BoardView(Block.COUNT);
		
		// instantiate click event handler only one time to save instantiation time
		EventHandler<MouseEvent> onCellViewClicked = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				boardView.requestFocus();
				CellView cellView = (CellView) event.getSource();
				moveFocusTo(cellView.row, cellView.col);
			}
		};
		
		// apply click event handler to all cells
		board.forEach(cell -> boardView.getCellView(cell).setOnMouseClicked(onCellViewClicked));
		
		// apply key press handler to the board (not each individual cell)
		boardView.setOnKeyPressed(this);
		
		// instantiate timer
		timer = new TimerWidget();
		
		// init check box for toggling hint mode
		hintMode = new CheckBox("Pencil (SHIFT)");
		hintMode.setFocusTraversable(false);
		autoMode = new CheckBox("Automation");
		autoMode.setFocusTraversable(false);
		autoMode.setSelected(true);
		remainMode = new CheckBox("Show remaining sum");
		remainMode.setFocusTraversable(false);
		remainMode.setSelected(true);
		
		// listener for hint mode and remain mode
		hintMode.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					boardView.setHintsCursor();
				}
				else {
					boardView.setValueCursor();
				}
			}
		});
		
		remainMode.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				toggleGroupSumMode(newValue);
			}
		});
		
		VBox controlBox = new VBox(hintMode, autoMode, remainMode);
		controlBox.setPadding(new Insets(0, 10, 0, 50));
		controlBox.setSpacing(4);
		
		// init combination list view
		combinationView = new CombinationView();
		
		// init combinator view
		combinator = new CombinatorWidget();
		
		
		
		// organize all components in grid style
		view = new GridPane();
		view.setBackground(BackgroundFactory.create(Color.rgb(200, 230, 200)));
		view.add(boardView, 0, 0);
		controlPanel = new VBox(timer, controlBox, combinationView, combinator);
		controlPanel.setAlignment(Pos.TOP_CENTER);
		view.add(controlPanel, 1, 0);
	}
	
	
	
	@Override
	public void handle(KeyEvent event) {
		Cell current = board.getCurrentCell();
		final int row = current.row;
		final int col = current.col;
		
		int nextRow = row;
		int nextCol = col;
		
		KeyCode code = event.getCode();
		switch (code) {
		// move using AWSD feature
		case UP:
		case W:
			nextRow = row == Board.MIN_INDEX ? Board.MAX_INDEX : row - 1;
			break;
			
		case DOWN:
		case S:
			nextRow = row == Board.MAX_INDEX ? Board.MIN_INDEX : row + 1;
			break;
			
		case LEFT:
		case A:
			nextCol = col == Board.MIN_INDEX ? Board.MAX_INDEX : col - 1;
			break;
			
		case RIGHT:
		case D:
			nextCol = col == Board.MAX_INDEX ? Board.MIN_INDEX : col + 1;
			break;
			
		
		
		// switch to hint mode feature
		case SHIFT:
			hintMode.setSelected(!hintMode.isSelected());
			break;
			
		// auto solve
		case SPACE:
			auto();
			break;
			
		// undo
		case Z:
			if (event.isControlDown()) {
				undo();
			}
			break;
	
		// delete cell feature
		case DIGIT0:
		case NUMPAD0:
		case DELETE:
		case BACK_SPACE:
			if (hintMode.isSelected()) {
				removeCellHints(row, col);
			}
			else {
				removeCellValue(row, col);
			}
			break;
			
		// enter cell value feature
		default:
			if (code.isDigitKey()) {
				String text = event.getText();
				int value = Integer.parseInt(text);
				
				if (hintMode.isSelected()) {
					toggleCellHint(row, col, value);
				}
				else {
					updateCellValue(row, col, value);
				}
			}
			break;
		}
		
		// move to next cell
		event.consume();	// prevent arrow keys automatically change the focus
		if (nextRow != current.row || nextCol != current.col) {
			moveFocusTo(nextRow, nextCol);
		}
	}
	
	
	
	private void moveFocusTo(int row, int col) {
		
		// update combination list if new group is selected
		Cell cell = board.getCell(row, col);
		int groupId = cell.getGroupId();
		if (groupId != board.getCurrentCell().getGroupId()) {
			combinationView.update(board.getGroup(groupId));
		}
		
		// don't update current cell before checking group ID changed
		board.setCurrentCell(row, col);
		boardView.focusOn(row, col);
	}
	

	
	private void updateGroupSumView(int groupId) {
		// if remain mode is not selected, view of group sum is never changed
		if (remainMode.isSelected()) {
			Group group = board.getGroup(groupId);
			boardView.updateGroupSum(group, true);
		}
	}
	
	private void removeCellValue(int row, int col) {
		Cell cell = board.getCell(row, col);
		
		// not allow to clear cell when it is locked
		if (cell.isLocked() || cell.isEmpty()) {
			return;
		}
		
		Step step = board.removeCellValue(row, col);
		boardView.updateCellView(cell);
		
		// update other cells which is affected by this cell
		for (CellSnapshot snapshot : step.snapshots) {
			Cell affectedCell = board.getCell(snapshot.row, snapshot.col);
			if (affectedCell.isLocked()) {
				continue;
			}
			boardView.getCellView(affectedCell).becomeValid();
		}
		
		steps.push(step);
		
		// current sum of group is changed
		updateGroupSumView(cell.getGroupId());
	}
	
	private void updateCellValue(int row, int col, int value) {
		Cell cell = board.getCell(row, col);
		
		// not allows to update cell value when cell is locked, or the new value is not changed
		if (cell.isLocked() || cell.getValue() == value) {
			return;
		}
		
		Step step = board.updateCellValue(row, col, value, autoMode.isSelected());
		boardView.updateCellView(cell);
		
		for (CellSnapshot snapshot : step.snapshots) {
			Cell affectedCell = board.getCell(snapshot.row, snapshot.col);
			boardView.updateCellView(affectedCell);
		}
		
		steps.push(step);
		
		// current sum of group is changed
		updateGroupSumView(cell.getGroupId());
		
		// check if game is solved
		checkGameIsSolved();
	}
	
	private void toggleCellHint(int row, int col, int hint) {
		Cell cell = board.getCell(row, col);
		
		// not allow to toggle hint when cell is holding value
		if (cell.isEmpty() == false) {
			return;
		}
		
		boolean isAdding = !board.getCell(row, col).getHints().contains(hint);
		Step step = isAdding ? board.addCellHint(row, col, hint) : board.subCellHint(row, col, hint);
		steps.push(step);
		
		boardView.getCellView(row, col).toggleHint(hint, isAdding);
	}
	
	private void removeCellHints(int row, int col) {
		Cell cell = board.getCell(row, col);
		
		// if cell doesn't any hint, don't have to clear it
		if (cell.getHints().isEmpty()) {
			return;
		}
		
		Step step = board.removeCellHints(row, col);
		steps.push(step);
		
		boardView.updateCellView(cell);
	}
	
	private void auto() {
		Step step = null;
		try {
			step = board.auto();
		}
		catch (Exception e) {
			logger.error("Exception in auto?", e);
			AlertFactory.createError(e).showAndWait();
		}
		
		// didn't solve anything, don't remember this step
		if (step == null) {
			return;
		}
		
		steps.push(step);
		
		Set<Integer> groupIds = new HashSet<Integer>();
		for (CellSnapshot snapshot : step.snapshots) {
			Cell affectedCell = board.getCell(snapshot.row, snapshot.col);
			if (snapshot.isMain()) {
				groupIds.add(affectedCell.getGroupId());
			}
			boardView.updateCellView(affectedCell);
		}
		
		for (int groupId : groupIds) {
			updateGroupSumView(groupId);
		}
		
		// after auto, game might be solved
		checkGameIsSolved();
	}
	
	private void undo() {
		if (steps.isEmpty()) {
			logger.warn("There is nothing to undo.");
			return;
		}
		
		Step step = steps.pop();
		Set<Integer> groupIds = board.undo(step);
		
		// update all affected group sum view
		for (int groupId : groupIds) {
			updateGroupSumView(groupId);
		}
		
		// update all cell view
		for (CellSnapshot snapshot : step.snapshots) {
			Cell cell = board.getCell(snapshot.row, snapshot.col);
			boardView.updateCellView(cell);
		}
	}
	
	
	
	private void toggleGroupSumMode(boolean remainMode) {
		for (Group group : board.getGroups()) {
			if (group.getTotal() != 0) {
				boardView.updateGroupSum(group, remainMode);
			}
		}
	}
	
	private void updateEntireView() {
		
		// upadte board view entirely
		boardView.update(board, remainMode.isSelected());
		
		// redraw combination view
		Group group = board.getGroup(board.getCurrentCell().getGroupId());
		combinationView.update(group);
	}
	
	//================================================================
	// Timer feature, START
	//================================================================
	public void startTimer() {
		timer.start();
	}
	
	public void pauseTimer() {
		timer.pause();
	}
	//================================================================
	// Timer feature, END
	//================================================================
	
	
	
	private void checkGameIsSolved() {
		if (board.isSolved() == false) {
			return;
		}
		
		// clear UNDO stack
		steps.clear();
		
		// stop the timer
		pauseTimer();
		
		// stop showing combination list
		combinationView.update(new Group(Group.NON_ID));
		
		// prevent user input
		boardView.setOnKeyPressed(null);
		
		// congratulate user
		boardView.congratulate();
		
		// pop up alert
		AlertFactory.createInfo("SUCCESS", null, "Congratulation! You are genius!").showAndWait();
	}
	
	
	
	public void importFrom(InputStream stream) throws IOException {
		// 1. wipe the view
		boardView.reset();
		
		// 2. instantiate new model and load new data
		board = new Board();
		board.loadFrom(stream);
		board.calculateAllHints();
		
		// 3. redraw view
		updateEntireView();
		
		// 4. setup controller
		boardView.setOnKeyPressed(this);
		boardView.requestFocus();
		
		// 5. reset timer
		timer.reset();
	}
	
	public void importFrom(File file) throws IOException {
		InputStream stream = new FileInputStream(file);
		importFrom(stream);
	}
	
	public void save() throws IOException {
		LevelUtils.saveCurrentGame(board);
	}
	
	public void loadSavedGame() throws IOException {
		board = LevelUtils.resumeSavedGame();
		updateEntireView();
	}
	
	
	
	public void exportTo(InputStream stream) {
		// TODO
		//board.saveTo(file);
	}
	
	
	
	// TODO later
	public void solve() {
		// try brute force solving first
	}
}
