package dev.ken.red.util;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import dev.ken.red.model.Board;

/**
 * Gonna do some fancy stuffs relate to game levels here.
 * @author kenguyen
 *
 */
public class LevelUtils {
	public static final String FILE_EXT = ".ksd";
	
	private static final String SAVE_PATH = "./save/last" + FILE_EXT;
	
	public static Set<String> listAllLevels() {
		Configuration configuration = new ConfigurationBuilder()
			.setUrls(ClasspathHelper.forPackage("dev.ken.red"))
			.setScanners(new ResourcesScanner());
		Reflections reflections = new Reflections(configuration);
		Set<String> levels = reflections.getResources(Pattern.compile(".*\\.ksd"));
		return levels;
	}
	
	public static void saveCurrentGame(Board board) throws IOException {
		File saveFile = new File(SAVE_PATH);
		saveFile.getParentFile().mkdirs();
		saveFile.createNewFile();
		board.saveTo(saveFile);
	}
	
	public static Board resumeSavedGame() throws IOException {
		Board board = new Board();
		File saveFile = new File(SAVE_PATH);
		if (saveFile.isFile()) {
			board.loadFrom(saveFile);
		}
		else {
			throw new IOException("Unable to find saved game.");
		}
		return board;
	}
}
