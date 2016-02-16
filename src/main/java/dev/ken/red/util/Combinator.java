package dev.ken.red.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Please not try to read the code of this class.
 * If you ever try to read it, don't try to understand it.
 * I'm not guarantee if anything would happen to your brain.
 * 
 * @author Ken Nguyen
 *
 */
public class Combinator {
	private static final int MIN = 1;
	private static final int MAX = 9;
	
	private final int size;
	private final int sum;
	
	private Set<int[]> combinations = new LinkedHashSet<int[]>();
	private Stack<Integer> buffer = new Stack<Integer>();
	private int total = 0;
	
	private Combinator(int size, int sum) {
		this.size = size;
		this.sum = sum;
	}
	
	private Set<int[]> calculate() {
		find(MIN - 1);
		return combinations;
	}
	
	private void find(int current) {
		if (buffer.size() == size - 1) {
			buffer.push(sum - total);
			int[] combination = new int[size];
			int i = 0;
			for (int value : buffer) {
				combination[i++] = value;
			}
			combinations.add(combination);
			buffer.pop();
			return;
		}
		
		int remain = sum - total;
		int more = size - buffer.size();
		int rest = more - 1;
		
		// min = remain - max of the rest
		// max of the rest = (first + last) * (size / 2)
		// size is the count of the rest
		int min = remain - MAX * rest + rest * (rest - 1) / 2;
		if (min <= current) {	// this step makes sure min always > 0
			min = current + 1;
		}
		
		// max = average - offset
		// average = remain / more
		// offset = more / 2
		int max = Math.round((float) (remain * 2 - more * more) / (more * 2));
		
		// start branching off
		for (int next = min; next <= max; next++) {
			total += buffer.push(next);
			find(next);
			total -= buffer.pop();
		}
	}
	
	public static Set<int[]> calculate(int size, int sum) {
		return new Combinator(size, sum).calculate();
	}
}
