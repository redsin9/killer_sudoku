package dev.ken.red.dlx;

import java.util.Collection;

import dev.ken.red.model.Board;
import dev.ken.red.model.Group;

/**
 * 
 * @author Ken
 *
 */
public class Converter {
	
	public static int[][] toGrid(Collection<Node> nodes, int size) {
		int[][] grid = new int[size][size];
		for (Node node : nodes) {
			grid[node.row][node.col] = node.val + 1;
		}
		return grid;
	}
	
	public static Board toBoard(Collection<Node> nodes) {
		Board board = new Board();
		Group group = new Group(Group.NON_ID);
		board.addGroup(group);
		
		for (Node node : nodes) {
			board.updateCellValue(node.row, node.col, node.val + 1, false);
		}
		
		return board;
	}
}
