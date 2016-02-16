package dev.ken.red.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import dev.ken.red.model.Board;
import dev.ken.red.model.Cell;
import dev.ken.red.model.undo.CellSnapshot;
import dev.ken.red.model.undo.Step;

/**
 * 
 * @author Ken
 *
 */
public class BoardTest {
	private static File dir;
	
	@BeforeClass
	public static void initTestDir() {
		dir = new File("./target/test/");
		dir.mkdirs();
	}
	
	@Test
	public void testLoadAndSave() throws IOException {
		Board board = new Board();
		
		URL url = this.getClass().getResource("/sample.ksd");
		File i = new File(url.getFile());
		board.loadFrom(i);
		
		File o = new File(dir.getPath() + "/output.ksd");
		o.createNewFile();
		board.saveTo(o);
	}
	
	@Test
	public void testAutoAndUndo() throws IOException {
		Board board = new Board();
		board.loadFrom(this.getClass().getResourceAsStream("/sample.ksd"));
		
		// fill all hints with value 1 - 9
		board.forEach(cell -> {
			for (int i = 1; i <= 9; i++) {
				board.addCellHint(cell.row, cell.col, i);
			}
		});
		
		Cell target = board.getCell(4, 4);
		int value = 1;
		
		// test auto mode when updating value
		Step step = board.updateCellValue(target.row, target.col, value, true);
		
		// step must contains all relative cells of (4,4) and itself
		Set<Cell> relativeCells = board.findRelativeCellsOf(target);
		assertEquals("Step doesn't snap expected number of cells.", relativeCells.size() + 1, step.snapshots.size());
		for (Cell relativeCell : board.findRelativeCellsOf(target)) {
			assertFalse("Automation mode doesn't clear hint as expected.", relativeCell.getHints().contains(value));
		}
		
		// test undo feature
		board.undo(step);
		
		// verify that all cell value has been restored as expected
		for (CellSnapshot snapshot : step.snapshots) {
			Cell cell = board.getCell(snapshot.row, snapshot.col);
			assertEquals("After undo, cell doesn't have expected value in snapshot.", snapshot.value, cell.getValue());
			Set<Integer> hints = cell.getHints();
			assertEquals("After undo, cell doesn't have expected hints in snapshot", snapshot.hints.length, hints.size());
			for (int hint : snapshot.hints) {
				assertTrue("After undo, cell hints doesn't have expected value.", hints.contains(hint));
			}
		}
	}
}
