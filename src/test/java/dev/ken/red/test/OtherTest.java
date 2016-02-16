package dev.ken.red.test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtherTest {
	private static final Logger logger = LoggerFactory.getLogger(OtherTest.class);
	
	@Ignore
	@Test
	public void testCharToInt() {
		for (char c = '1'; c <= '9'; c++) {
			int hint = Character.digit(c, 10);
			logger.debug("Hint value: " + hint);
		}
	}
	
	@Ignore
	@Test
	public void stupidTest() {
		definitionOfStupidity("");
	}
	
	private void definitionOfStupidity(String s) {
		if (s == "") {
			logger.debug("String is empty");
		}
		else {
			logger.debug("You are stupid.");
		}
	}
	
	@Ignore
	@Test
	public void sizeCalculation() {
		for (int size = 2; size < 9; size++) {
			int min = (size * size + size) / 2;
			int max = (19 * size - size * size) / 2;
			int length = max - min;
			logger.debug(min + " - " + max + " - " + length);
		}
	}
	
	@Ignore
	@Test
	public void stream() {
		Collection<Integer> numbers = new LinkedList<Integer>();
		for (int i = -9; i < 9; i++) {
			numbers.add(i);
		}
		numbers.stream().forEach(number -> logger.debug("number: " + number));
	}
	
	@Ignore
	@Test
	public void testUUIDtime() {
		long t0, t1, span, random;
		
		t0 = System.nanoTime();
		random = UUID.randomUUID().getMostSignificantBits();
		t1 = System.nanoTime();
		span = (t1 - t0) / 1000;
		logger.debug("Random: " + random);
		logger.debug("Span: " + span + " us");
		
		t0 = System.nanoTime();
		random = UUID.randomUUID().getMostSignificantBits();
		t1 = System.nanoTime();
		span = (t1 - t0) / 1000;
		logger.debug("Random: " + random);
		logger.debug("Span: " + span + " us");
	}
}
