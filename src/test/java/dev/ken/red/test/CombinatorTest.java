package dev.ken.red.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ken.red.util.Combinator;

public class CombinatorTest {
	private static final Logger logger = LoggerFactory.getLogger(CombinatorTest.class);
	
	private void testPattern(int size, int sum, int expectedCount) {
		logger.debug("Calculating combinations with size=" + size + " & sum=" + sum);
		
		long t0 = System.nanoTime();
		Set<int[]> combinations = Combinator.calculate(size, sum);
		assertEquals(expectedCount, combinations.size());
		for (int[] combination : combinations) {
			logger.debug(Arrays.toString(combination));
			assertEquals(size, combination.length);
			int total = 0;
			for (int value : combination) {
				total += value;
			}
			assertEquals(sum, total);
		}
		long total = System.nanoTime() - t0;
		long milli = total / 1000000;
		long remain = total % 1000000;
		long micro = remain / 1000;
		long nano = remain % 1000;
		logger.debug(String.format("Took [%d]ms [%d]us [%d]ns", milli, micro, nano));
	}

	@Test
	public void normalTest() {
		testPattern(2, 10, 4);
		testPattern(3, 10, 4);
		testPattern(4, 10, 1);
		testPattern(4, 11, 1);
		testPattern(4, 20, 12);
		testPattern(5, 19, 5);
		testPattern(5, 20, 6);
	}
	
	@Test
	public void faultyTest() {
		testPattern(2, 18, 0);
		testPattern(5, 10, 0);
	}
}
