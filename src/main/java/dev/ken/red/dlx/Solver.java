package dev.ken.red.dlx;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import dev.ken.red.model.Board;

/**
 * 
 * @author Ken
 *
 */
public class Solver {
	
	private final DLX dlx;
	private final List<Collection<Node>> solutions = new LinkedList<>();
	
	private Solver(int blockCount, int blockSize, Board board) {
		dlx = new DLX(blockCount, blockSize);
		dlx.applyBoardConstraints(board);
	}
	
	private List<Collection<Node>> run() {
		find(dlx.main.next);
		return solutions;
	}
	
	private void find(Head head) {
		
		// go from first candidate to last candidate
		Node candidate = head.first;
		do {
			// cover row of that candidate, if it is good, keep going until we find the solution
			Boolean result = dlx.cover(candidate);
			
			// result == null means this candidate is the last cell of a group
			// but it is too big or too small
			if (result == null) {
				// TODO we can calculate the correct value here
				candidate = candidate.D;
				continue;
			}
			
			if (result == true) {
				
				// find next smallest head in the head list
				Head smallest = dlx.findSmallestHead();
				
				// if there is no more head in the list, the board is solved
				if (smallest == dlx.main) {
					// save the solution
					solutions.add(dlx.getSteps());
					
					// return so the caller can try other candidates
					dlx.uncover();
					return;
				}
				
				// continue recursively until the board is solved
				find(smallest);
			}
			
			// always try next candidate
			dlx.uncover();
			
			candidate = candidate.D;
		} while (candidate != head.first);
	}
	
	
	
	public static List<Collection<Node>> solve(int blockCount, int blockSize, Board board) {
		return new Solver(blockCount, blockSize, board).run();
	}
}
