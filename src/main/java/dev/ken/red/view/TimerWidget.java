package dev.ken.red.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * 
 * @author Ken
 *
 */
public class TimerWidget extends HBox {
	private static final Font FONT;
	static {
		// FONT CREDIT: http://www.1001fonts.com/digital-dismay-font.html#styles
		String url = TimerWidget.class.getResource("/fonts/digital-dismay.regular.otf").toExternalForm();
		FONT = Font.loadFont(url, 48);
	}
	
	private int h = 0;
	private int m = 0;
	private int s = 0;
	
	private Label hh = new Label("00");
	private Label mm = new Label("00");
	private Label ss = new Label("00");
	
	private Timeline timeline;
	
	public TimerWidget() {
		super.setPadding(new Insets(16));
		super.setAlignment(Pos.CENTER);
		
		Label s1 = new Label(":");
		s1.setFont(FONT);
		Label s2 = new Label(":");
		s2.setFont(FONT);
		super.getChildren().addAll(hh, s1, mm, s2, ss);
		
		hh.setFont(FONT);
		mm.setFont(FONT);
		ss.setFont(FONT);
		
		timeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
			s++;
			if (s == 60) {
				s = 0;
				m++;
				if (m == 60) {
					m = 0;
					h++;
					hh.setText(String.format("%02d", h));
				}
				mm.setText(String.format("%02d", m));
			}
			ss.setText(String.format("%02d", s));
	    }));
	    timeline.setCycleCount(Animation.INDEFINITE);
	}
	
	public void start() {
		timeline.play();
	}
	
	public void pause() {
		timeline.stop();
	}
	
	public void reset() {
		h = m = s = 0;
		hh.setText("00");
		mm.setText("00");
		ss.setText("00");
	}
}
