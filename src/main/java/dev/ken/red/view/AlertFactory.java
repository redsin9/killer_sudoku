package dev.ken.red.view;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * fast way to create JavaFX alert
 * @author kenguyen
 *
 */
public class AlertFactory {
	
	public static Alert createInfo(String title, String header, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		
		return alert;
	}
	
	public static Alert createError(Exception e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("ERROR");
		alert.setHeaderText(e.getMessage());
		
		// TODO might filter and print out full stack trace
		StackTraceElement element = e.getStackTrace()[0];
		StringBuilder sb = new StringBuilder("Exception ")
			.append(e.getClass().getName()).append(" at ")
			.append(element.getClassName()).append(".")
			.append(element.getMethodName()).append("(")
			.append(element.getFileName()).append(":")
			.append(element.getLineNumber()).append(")");
		alert.setContentText(sb.toString());
		
		return alert;
	}
}
