package dev.ken.red.view;

import java.util.Set;

import dev.ken.red.util.Combinator;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Widget for calculating custom combinations
 * 
 * @author kenguyen
 *
 */
public class CombinatorWidget extends GridPane {
	private static final Font FONT = Font.font("Consolas", FontWeight.BOLD, 14);
	
	private CustomSlider sizeSlider;
	private CustomSlider sumSlider;
	private VBox list = new VBox();
	
	
	
	public CombinatorWidget() {
		super.setAlignment(Pos.CENTER);
		super.setVgap(10);
		super.setHgap(20);
		super.setPadding(new Insets(0, 10, 0, 10));
		
		// sum slider changes dynamically based on value of size slider
		sumSlider = new CustomSlider("Group sum");
		sumSlider.setMinWidth(250);
		super.add(sumSlider, 0, 0, 2, 1);
		
		sizeSlider = new CustomSlider("Group size", 2, 8, 5);
		sizeSlider.setMajorTickUnit(1);		// show major tick for each value
		sizeSlider.setMinorTickCount(0);	// don't show minor tick
		sizeSlider.setOrientation(Orientation.VERTICAL);
		sizeSlider.setMinHeight(175);
		sizeSlider.setMaxHeight(175);
		super.add(sizeSlider, 0, 1);
		
		updateSumSlider(5);
		
		list.setAlignment(Pos.TOP_CENTER);
		list.setPadding(new Insets(0, 20, 0, 0));
		list.setSpacing(-3);
		super.add(list, 1, 1);
		
		// whenever size is changed, update the sum slider
		sizeSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
			private int previousSize = sizeSlider.getRoundValue();
			
			@Override
			public void handle(MouseEvent event) {
				int currentSize = sizeSlider.getRoundValue();
				if (currentSize != previousSize) {
					updateSumSlider(currentSize);
					previousSize = currentSize;
				}
			}
		});
		
		// whenever sum is change, calculate new combinations
		sumSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				updateCombinations(sizeSlider.getRoundValue(), sumSlider.getRoundValue());
			}
		});
	}
	
	private void updateSumSlider(int size) {
		int min = (size * size + size) / 2;
		int max = (19 * size - size * size) / 2;
		int value = (max + min) / 2;	// always put value at the middle
		
		sumSlider.setMin(min);
		sumSlider.setMax(max);
		sumSlider.setValue(value);
		
		int unit = 0;
		int length = max - min;
		switch (length) {
		case 8:
			unit = 1;
			break;
			
		case 14:
			unit = 2;
			break;
			
		case 18:
			unit = 3;
			break;
			
		case 20:
			unit = 4;
			break;
		}
		
		sumSlider.setMajorTickUnit(unit);
		sumSlider.setMinorTickCount(unit - 1);
		
		updateCombinations(sizeSlider.getRoundValue(), sumSlider.getRoundValue());
	}
	
	private void updateCombinations(int size, int sum) {
		Set<int[]> combinations = Combinator.calculate(size, sum);
		list.getChildren().clear();
		combinations.stream().forEach(combination -> {
			StringBuilder sb = new StringBuilder();
			for (int value : combination) {
				sb.append(value).append(" ");
			}
			Label output = new Label(sb.toString());
			output.setFont(FONT);
			list.getChildren().add(output);
		});
	}
	
	
	
	/**
	 * This slider will never take focus. It also comes with some predefine settings.
	 * 
	 * @author kenguyen
	 *
	 */
	private class CustomSlider extends Slider {
		public CustomSlider(String tooltip) {
			super();
			super.setFocusTraversable(false);
			super.setShowTickMarks(true);
			super.setSnapToTicks(true);
			super.setShowTickLabels(true);
			super.setTooltip(new Tooltip(tooltip));
		}
		
		public CustomSlider(String tooltip, int min, int max, int value) {
			this(tooltip);
			super.setMin(min);
			super.setMax(max);
			super.setValue(value);
		}
		
		@Override
		public void requestFocus() {
			// this method will prevent the node from taking focus
		}
		
		public int getRoundValue() {
			return (int)super.getValue();
		}
	}
}
