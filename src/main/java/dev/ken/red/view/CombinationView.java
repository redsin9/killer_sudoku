package dev.ken.red.view;

import dev.ken.red.model.Combination;
import dev.ken.red.model.Group;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * 
 * @author Ken
 *
 */
public class CombinationView extends VBox {
	private static final Font FONT = new Font("Consolas", 18);
	
	private VBox list;
	
	public CombinationView() {
		super.setPadding(new Insets(20));
		super.setSpacing(10);
		super.setAlignment(Pos.TOP_CENTER);
		super.setMinSize(CellView.SIZE * 3, CellView.SIZE * 4);
		super.setMaxSize(CellView.SIZE * 3, CellView.SIZE * 4);
		
		Text title = new Text("Combination(s)");
		title.setFont(FONT);
		list = new VBox();
		list.setSpacing(1);
		list.setAlignment(Pos.CENTER);
		
		super.getChildren().addAll(title, list);
	}
	
	public void update(Group group) {
		list.getChildren().clear();
		group.getCombinations().stream().forEach(combination -> {
			Line line = new Line(combination);
			list.getChildren().add(line);
		});
	}
	
	
	
	private static class Line extends Text implements EventHandler<MouseEvent> {
		private static final Color POSSIBLE = Color.BLACK;
		private static final Color DISABLED = Color.grayRgb(200);
		
		private final Combination combination;
		
		public Line(Combination combination) {
			this.combination = combination;
			super.setFont(FONT);
			StringBuilder sb = new StringBuilder();
			for (int value : combination.values) {
				sb.append(value).append(" ");
			}
			super.setText(sb.toString());
			super.setOnMouseClicked(this);
			this.update(combination.isOn());
		}
		
		private void update(boolean isOn) {
			if (isOn) {
				super.setFill(POSSIBLE);
			}
			else {
				super.setFill(DISABLED);
			}
		}

		@Override
		public void handle(MouseEvent event) {
			if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
	        	combination.toggle(!combination.isOn());
	        	update(combination.isOn());
	        }
		}
	}
}
