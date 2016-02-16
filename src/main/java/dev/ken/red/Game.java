package dev.ken.red;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ken.red.controller.GameController;
import dev.ken.red.util.LevelUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * GUI
 * 
 * @author Ken
 *
 */
public class Game extends Application {
	private static final Logger logger = LoggerFactory.getLogger(Game.class);
	
	private Stage stage;
	private GameController game;

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		GridPane root = new GridPane();
		
		logger.debug("Instantiating menu bar");
		MenuBar menuBar = initMenuBar();
		root.add(menuBar, 0, 0);
		
		logger.info("Instantiating sample board for demo");
		initGame();
		root.add(game.view, 0, 1);
		
		logger.info("Setting up stage");
		setupStage(root);
		stage.show();
		
		logger.debug("Start counting game time.");
		game.startTimer();
	}
	
	private MenuBar initMenuBar() {
		MenuBar menuBar = new MenuBar();
		
		Menu menuFile = new Menu("File");
		MenuItem menuFileOpen = new MenuItem("Open");
		MenuItem menuFileSave = new MenuItem("Save");
		menuFile.getItems().addAll(menuFileOpen, menuFileSave);
		
		Menu menuHelp = new Menu("Help");
		MenuItem menuHelpAbout = new MenuItem("About");
		menuHelp.getItems().add(menuHelpAbout);
		
		menuBar.getMenus().addAll(menuFile, menuHelp);
		
		// hook up controls
		menuFileOpen.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// TODO filter .ksd file only
				FileChooser fc = new FileChooser();
				File file = fc.showOpenDialog(stage);
				try {
					game.importFrom(file);
				} 
				catch (IOException e) {
					logger.error("Failed to import game from file.", e);
				}
			}
		});
		
		menuFileSave.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					game.save();
				} 
				catch (IOException e) {
					logger.error("Failed to save game.", e);
				}
			}
		});
		
		return menuBar;
	}
	
	private void initGame() {
		game = new GameController();
		
		logger.info("Try to resume the last saved game.");
		try {
			game.loadSavedGame();
			return;
		}
		catch (Exception e) {
			logger.info("Unable to resume game due to: " + e.getMessage());
		}
		
		logger.info("Looking for all available game levels.");
		Set<String> levels = LevelUtils.listAllLevels();
		if (levels.isEmpty()) {
			logger.warn("Game is not packaged with any level.");
			return;
		}
		
		logger.info("Loading the first game level.");
		String firstLevel = levels.iterator().next();
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(firstLevel);
		try {
			game.importFrom(stream);
		} 
		catch (IOException e1) {
			logger.error("IOException while loading sample game!", e1);
		}
	}
	
	private void setupStage(Parent view) {
		Scene scene = new Scene(view);
		stage.setScene(scene);
		stage.setTitle("Red Killer");
		stage.setResizable(false);
		stage.sizeToScene();
		
		// save game when app is closed
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				game.pauseTimer();
				try {
					game.save();
				} 
				catch (IOException e) {
					logger.error("Failed to save game.", e);
				}
				finally {
					//com.sun.javafx.application.PlatformImpl.tkExit();
					Platform.exit();
					System.exit(0);
				}
			}
		});
		
		// pause game when app is minimized
		stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean minimized) {
				if (minimized) {
					game.pauseTimer();
				}
				else {
					game.startTimer();
				}
			}
		});
	}
	
	public static void main(String[] args) {
		logger.info("Launching application");
		Application.launch(args); 
	}
}
