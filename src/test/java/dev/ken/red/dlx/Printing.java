package dev.ken.red.dlx;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import dev.ken.red.model.Board;
import dev.ken.red.model.Cell;
import dev.ken.red.model.Group;

public class Printing {
	
	private void printGrid(int[][] grid, int blockSize) {
		for (int row = 0; row < grid.length; row++) {
			StringBuilder line = new StringBuilder();
			for (int col = 0; col < grid[row].length; col++) {
				int value = grid[row][col];
				if (value == 0) {
					line.append("_ ");
				}
				else {
					line.append(value).append(" ");
				}
				
				if ((col + 1) % blockSize == 0) {
					line.append("  ");
				}
			}
			
			if ((row + 1) % blockSize == 0) {
				line.append("\n");
			}
			
			log(line.append("\n"));
		}
	}
	
	@Test
	public void printRandom() {
		final int blockCount = 2;
		final int blockSize = 2;
		Board board = new Board();
		int[][] coords = {
				{0,0},{0,1}
		};
		Group group = new Group(9);
		for (int[] coord : coords) {
			Cell cell = new Cell(coord[0], coord[1]);
			group.addCell(cell);
		}
		group.setSum(3);
		board.addGroup(group);
		
		Collection<Node> nodes = Randomizer.generate(blockCount, blockSize, board);
		int[][] grid = Converter.toGrid(nodes, blockCount * blockSize);
		printGrid(grid, blockSize);
	}
	
	@Test
	public void printSolver() {
		final int blockCount = 2;
		final int blockSize = 2;
		Board board = new Board();
		int[][] coords = {
				{0,1},{0,2},{1,2}
		};
		Group group = new Group(9);
		for (int[] coord : coords) {
			Cell cell = new Cell(coord[0], coord[1]);
			group.addCell(cell);
		}
		group.setSum(6);
		board.addGroup(group);
		
		List<Collection<Node>> solutions = Solver.solve(blockCount, blockSize, board);
		System.out.println("Found " + solutions.size() + " solution(s)");
		solutions.stream().forEach(solution -> {
			int[][] grid = Converter.toGrid(solution, blockCount * blockSize);
			printGrid(grid, blockSize);
			System.out.println("{<=- o0o -=>}\n");
		});
	}
	
	
	
	@Test
	public void printSampleMatrix() {
		final int blockCount = 2;
		final int blockSize = 2;
		final int boardSize = blockCount * blockSize;
		
		StringBuilder hr = new StringBuilder();
		int hrLength = boardSize * boardSize * 4 + boardSize * 4 + 14;
		for (int i = 0; i < hrLength; i++) {
			hr.append("-");
		}
		hr.append("\n");
		
		// print header
		StringBuilder line1 = new StringBuilder("      ");
		StringBuilder line2 = new StringBuilder("      ");
		for (int a = 0; a < 4; a++) {
			line1.append("| ");
			line2.append("| ");
			for (int b = 0; b < boardSize; b++) {
				line1.append(b);
				for (int c = 0; c < boardSize; c++) {
					line1.append(" ");
					line2.append(c);
				}
				line2.append(" ");
			}
		}
		line1.append("\n");
		line2.append("\n");
		log(line1);
		log(line2);
		
		// print body
		for (int row = 0; row < boardSize; row++) {
			for (int col = 0; col < boardSize; col++) {
				log(hr);
				for (int value = 0; value < boardSize; value++) {
					StringBuilder sb = new StringBuilder(String.format("%d,%d:%d ", row, col, value));
					for (int a = 0; a < 4; a++) {
						sb.append("| ");
						for (int b = 0; b < boardSize; b++) {
							for (int c = 0; c < boardSize; c++) {
								String appender = " ";
								switch (a) {
								case 0:
									if (row == b && col == c) {
										appender = "" + value;
									}
									break;
									
								case 1:
									if (row == b && value == c) {
										appender = "" + value;
									}
									break;
									
								case 2:
									if (col == b && value == c) {
										appender = "" + value;
									}
									break;
									
								case 3:
									if (value == c) {
										int blockRow = row / blockCount;
										int blockCol = col / blockCount;
										int blockIndex = blockRow * blockCount + blockCol;
										if (blockIndex == b) {
											appender = "" + value;
										}
									}
									break;
								}
								sb.append(appender);
							}
							sb.append(" ");
						}
					}
					sb.append("\n");
					log(sb);
				}
			}
		}
	}
	
	private void log(StringBuilder sb) {
		System.out.print(sb);
	}
}
