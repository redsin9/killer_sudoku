package dev.ken.red.view;

import dev.ken.red.model.Board;
import dev.ken.red.model.Group;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * 
 * @author Ken
 *
 */
public class CellView extends StackPane {
	public static final int SIZE = 75;		// in pixel
	private static final int OFFSET = 4;
	
	private static final Font VALUE_FONT = Font.font("Consolas", 36);
	private static final Font HINT_FONT = Font.font("Consolas", 14);
	private static final Font SUM_FONT = Font.font("Consolas", FontWeight.BOLD, FontPosture.ITALIC, 15);
	
	private static final Color CL_GROUP = Color.rgb(0, 150, 220);
	private static final Color CL_HINT = Color.rgb(0, 127, 0);
	private static final Color CL_SUM = Color.rgb(255, 127, 0);
	
	public final int row;
	public final int col;
	
	private Canvas background;
	private Canvas highlight;
	private Label value;
	private Label[][] hints;
	private Canvas border;
	private Label sum;	// TODO
	
	
	
	public CellView(int row, int col) {
		this.row = row;
		this.col = col;
		
		// setup GUI
		background = new Canvas(SIZE, SIZE);
		
		highlight = new Canvas(SIZE, SIZE);
		GraphicsContext gc = highlight.getGraphicsContext2D();
		gc.setFill(Color.LIME);
		gc.fillRect(0, 0, SIZE, SIZE);
		highlight.setVisible(false);
		
		value = new Label();
		value.setBackground(BackgroundFactory.createTransparent());
		value.setMinSize(SIZE, SIZE);
		value.setMaxSize(SIZE, SIZE);
		value.setAlignment(Pos.CENTER);
		value.setFont(VALUE_FONT);
		
		GridPane hintGrid = new GridPane();
		hintGrid.setPadding(new Insets(OFFSET * 2));
		hintGrid.setHgap(6);
		hintGrid.setVgap(-OFFSET);
		hintGrid.setAlignment(Pos.CENTER);
		hintGrid.setMinSize(SIZE, SIZE);
		hintGrid.setMaxSize(SIZE, SIZE);
		hints = new Label[3][3];
		Background transparent = BackgroundFactory.create(Color.rgb(0, 0, 0, 0));
		for (int i = 0; i < Board.SIZE; i++) {
			Label hint = new Label();
			hint.setFont(HINT_FONT);
			hint.setTextFill(CL_HINT);
			hint.setBackground(transparent);
			hint.setText(String.valueOf(i + 1));
			hint.setVisible(false);
			int hintRow = 2 - i / 3;
			int hintCol = i % 3;
			hintGrid.add(hint, hintCol, hintRow);
			hints[hintRow][hintCol] = hint;
		}
		
		border = new Canvas(SIZE, SIZE);
		
		super.getChildren().addAll(background, highlight, value, hintGrid, border);
	}
	
	// TODO find better way to support Level Generator
	public void setBackground(Color color) {
		GraphicsContext gc = background.getGraphicsContext2D();
		gc.setFill(color);
		gc.fillRect(0, 0, SIZE, SIZE);
	}
	
	protected void attachSum() {
		sum = new Label();
		sum.setMinSize(SIZE, SIZE);
		sum.setMaxSize(SIZE, SIZE);
		sum.setPadding(new Insets(OFFSET - 1, 0, 0, OFFSET + 1));
		sum.setAlignment(Pos.TOP_LEFT);
		sum.setFont(SUM_FONT);
		sum.setTextFill(CL_SUM);
		super.getChildren().add(sum);
	}
	
	protected void detachSum() {
		super.getChildren().remove(sum);
	}
	
	protected void toggleHighlight(boolean show) {
		highlight.setVisible(show);
	}
	
	protected void attachItem(Node item) {
		super.getChildren().add(item);
	}
	
	protected void detachItem(Node item) {
		super.getChildren().remove(item);
	}
	
	
	
	protected void drawValue(int value) {
		wipeHints();
		this.value.setText(String.valueOf(value));
	}
	
	protected void wipeValue() {
		value.setText("");
		becomeValid();
	}
	
	public String getValue() {
		return value.getText();
	}
	
	public boolean hasValue() {
		return !getValue().isEmpty();
	}
	
	public void becomeLocked() {
		value.setTextFill(Color.BLACK);
	}
	
	public void becomeError() {
		value.setTextFill(Color.RED);
	}
	
	public void becomeValid() {
		value.setTextFill(Color.BLUE);
	}
	
	protected void wipeBorder() {
		border.getGraphicsContext2D().clearRect(0, 0, SIZE, SIZE);
	}
	
	protected void drawBorder(Group group) {
		// determine relationship with all other cells around
		boolean isU = group.containsCellAt(row - 1, col);
		boolean isUR = group.containsCellAt(row - 1, col + 1);
		boolean isR = group.containsCellAt(row, col + 1);
		boolean isRD = group.containsCellAt(row + 1, col + 1);
		boolean isD = group.containsCellAt(row + 1, col);
		boolean isDL = group.containsCellAt(row + 1, col - 1);
		boolean isL = group.containsCellAt(row, col - 1);
		boolean isLU = group.containsCellAt(row - 1, col - 1);
		
		if (isUR && isRD && isDL && isLU) {
			return;
		}
		
		final int lo = OFFSET + 1;
		final int hi = lo * 2;
		final int size = SIZE - hi + 1;
		
		GraphicsContext gc = border.getGraphicsContext2D();
		gc.setFill(CL_GROUP);
		gc.fillRect(0, 0, SIZE, SIZE);
		gc.clearRect(lo, lo, size, size);
		
		if (isU) {
			gc.clearRect(lo, 00, size, size);
		}
		
		if (isU && isUR && isR) {
			gc.clearRect(hi, 00, size, size);
		}
		
		if (isR) {
			gc.clearRect(hi, lo, size, size);
		}
		
		if (isR && isRD && isD) {
			gc.clearRect(hi, hi, size, size);
		}
		
		if (isD) {
			gc.clearRect(lo, hi, size, size);
		}
		
		if (isD && isDL && isL) {
			gc.clearRect(00, hi, size, size);
		}
		
		if (isL) {
			gc.clearRect(00, lo, size, size);
		}
		
		if (isL && isLU && isU) {
			gc.clearRect(00, 00, size, size);
		}
	}
	
	protected void drawSum(int value) {
		sum.setText(String.valueOf(value));
	}
	
	protected void toggleSumColor(boolean isValid) {
		if (isValid) {
			sum.setTextFill(CL_SUM);
		}
		else {
			sum.setTextFill(Color.RED);
		}
	}
	
	private Label getHint(int value) {
		// support keypad in desktop keyboard which use layout
		// 7 8 9				1 2 3
		// 4 5 6	instead of 	4 5 6
		// 1 2 3				7 8 9
		// to switch to the style on the right side, remove the part '2 -' of hintRow
		int hintRow = 2 - (value - 1) / 3;
		int hintCol = (value - 1) % 3;
		return hints[hintRow][hintCol];
	}
	
	public void toggleHint(int value, boolean visible) {
		getHint(value).setVisible(visible);
	}
	
	public void wipeHints() {
		for (int i = 1; i <= Board.SIZE; i++) {
			toggleHint(i, false);
		}
	}
	
	
	
	@Override
	public String toString() {
		return String.format("(%d,%d)", row, col);
	}
	
	
	
	@Deprecated
	public void drawBorderOld(Group group) {
		
		// determine relationship with all other cells around
		boolean isU = group.containsCellAt(row - 1, col);
		boolean isUR = group.containsCellAt(row - 1, col + 1);
		boolean isR = group.containsCellAt(row, col + 1);
		boolean isRD = group.containsCellAt(row + 1, col + 1);
		boolean isD = group.containsCellAt(row + 1, col);
		boolean isDL = group.containsCellAt(row + 1, col - 1);
		boolean isL = group.containsCellAt(row, col - 1);
		boolean isLU = group.containsCellAt(row - 1, col - 1);
		
		GraphicsContext gc = border.getGraphicsContext2D();
		gc.clearRect(0, 0, border.getWidth(), border.getHeight());
		gc.setStroke(CL_GROUP);
		gc.setLineDashes(3);
		gc.setLineWidth(2);
		
		final int lo = OFFSET;
		final int hi = SIZE - OFFSET;
		
		int x1 = isL ? 0 : lo;
		int x2 = isR ? SIZE : hi;
		int y1 = isU ? 0 : lo;
		int y2 = isD ? SIZE : hi;
		
		// draw lines
		if (!isU) {
			gc.strokeLine(x1, lo, x2, lo);
		}
		
		if (!isR) {
			gc.strokeLine(hi, y1, hi, y2);
		}
		
		if (!isD) {
			gc.strokeLine(x1, hi, x2, hi);
		}
		
		if (!isL) {
			gc.strokeLine(lo, y1, lo, y2);
		}
		
		// draw corners
		if (isU && !isUR && isR) {
			gc.strokeLine(hi, lo, SIZE, lo);
			gc.strokeLine(hi, 0, hi, OFFSET);
		}
		
		if (isR && !isRD && isD) {
			gc.strokeLine(hi, hi, SIZE, hi);
			gc.strokeLine(hi, hi, hi, SIZE);
		}
		
		if (isD && !isDL && isL) {
			gc.strokeLine(0, hi, OFFSET, hi);
			gc.strokeLine(lo, hi, lo, SIZE);
		}
		
		if (isL && !isLU && isU) {
			gc.strokeLine(0, lo, OFFSET, lo);
			gc.strokeLine(lo, 0, lo, OFFSET);
		}
	}
}
