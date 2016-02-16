package dev.ken.red.view;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class BackgroundFactory {
	public static Background create(Color color) {
		BackgroundFill fill = new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY);
		return new Background(fill);
	}
	
	public static Background createTransparent() {
		Color color = Color.color(0, 0, 0, 0);
		return create(color);
	}
}
