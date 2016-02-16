package dev.ken.red.dlx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import dev.ken.red.model.Board;

/**
 * 
 * @author Ken
 *
 */
public class Randomizer {
	
	private final Random random = new Random(UUID.randomUUID().getMostSignificantBits());
	private final DLX dlx;
	
	private Randomizer(int blockCount, int blockSize, Board board) {
		dlx = new DLX(blockCount, blockSize);
		dlx.applyBoardConstraints(board);
	}
	
	private Collection<Node> run() {
		seed(dlx.main.next);
		return dlx.getSteps();
	}
	
	private boolean seed(Head head) {
		
		// make a candidate list
		List<Node> candidates = new ArrayList<Node>(head.getCount());
		Node candidate = head.first;
		do {
			candidates.add(candidate);
			candidate = candidate.D;
		} while (candidate != head.first);
		
		// try all possible candidates
		while (candidates.isEmpty() == false) {
			
			// pick a random candidate in the list and remove it
			int index = random.nextInt(candidates.size());
			candidate = candidates.get(index);
			candidates.remove(index);
			
			// cover row of that candidate
			Boolean result = dlx.cover(candidate);
			if (result == null) {
				// result == null means candidate didn't satisfy group sum constraints
				// in this case, do nothing, just let randomizer try the next candidate
				continue;
			}
			
			if (result == true) {
				// if this is good candidate, continue recursively
				Head smallest = dlx.findSmallestHead();
				if (smallest == dlx.main) {
					return true;
				}
				
				if (seed(smallest)) {
					return true;
				}
			}
			
			dlx.uncover();
		}
		
		// if code reach here, that means there is no good candidate
		return false;
	}
	
	
	
	public static Collection<Node> generate(int blockCount, int blockSize, Board board) {
		return new Randomizer(blockCount, blockSize, board).run();
	}
}
