package dev.ken.red;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ken.red.controller.ILevelGenerator;
import dev.ken.red.controller.LevelGenerator3;
import dev.ken.red.view.AlertFactory;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * GUI
 * 
 * @author Ken
 *
 */
public class Tool extends Application {
	private static final Logger logger = LoggerFactory.getLogger(Tool.class);
	
	private Stage stage;
	private ILevelGenerator generator;

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		GridPane root = new GridPane();
		
		logger.info("Instantiating controls");
		HBox toolbar = new HBox();
		toolbar.setPadding(new Insets(8));
		toolbar.setSpacing(8);
		Button exportButton = new Button("Export");
		exportButton.setFocusTraversable(false);
		toolbar.getChildren().addAll(exportButton);
		root.add(toolbar, 0, 0);
		
		logger.info("Instantiating blank board for creation");
		generator = new LevelGenerator3();
		root.add(generator.getView(), 0, 1);
		
		logger.info("Setting controls");
		exportButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				export();
			}
		});
		
		logger.info("Setting up scene");
		Scene scene = new Scene(root);
		
		logger.info("Setting up windows");
		stage.setTitle("RedSin");
		stage.setScene(scene);
		stage.setResizable(false);
		stage.sizeToScene();
		
		logger.info("Showing windows");
		stage.show();
	}
	
	private void export() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		
		String error = generator.validate();
		if (error != null) {
			AlertFactory.createInfo("Invalid Data", null, error).showAndWait();
			return;
		}
		
		FileChooser fc = new FileChooser();
		File file = fc.showSaveDialog(stage);
		if (file == null) {
			return;
		}
		
		try {
			generator.export(file);
		} 
		catch (Exception e) {
			e.printStackTrace();
			alert.setHeaderText("There is error while exporting the level.");
			alert.setContentText(e.getMessage());
			alert.showAndWait();
		}
	}
	
	
	
	public static void main(String[] args) {
		logger.info("Launching application");
		Application.launch(args); 
	}
}
