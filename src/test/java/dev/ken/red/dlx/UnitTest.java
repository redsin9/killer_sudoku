package dev.ken.red.dlx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ken.red.model.Board;

/**
 * 
 * @author Ken
 *
 */
public class UnitTest {
	private static final Logger logger = LoggerFactory.getLogger(UnitTest.class);
	
	private void testPattern(int blockCount, int blockSize) {
		final int boardSize = blockCount * blockSize;
		final int expectedHeadCount = boardSize * boardSize * 4;
		
		long t0 = System.nanoTime();
		Head main = new DLX(blockCount, blockSize).main;
		long span = System.nanoTime() - t0;
		float ms = (float) span / 1000000;
		logger.debug("Span: " + ms);
		logger.debug("{<=-- TEST -=>}");
		
		int headCount = 0;
		Head head = main.next;
		while (head != main) {
			headCount++;
			
			assertFalse("Head count exceeded the expected value.", headCount > expectedHeadCount);
			assertEquals("Head doesn't have expected row count.", boardSize, head.getCount());
			
			// traverse through all candidates of a head (column)
			int rowCount = 0;
			Node row = head.first;	// get pointer to the first candidate of the head
			do {
				rowCount++;
				assertFalse("Node count in a column is higher than expected.", rowCount > boardSize);
				
				// traverse through all nodes in the row of the candidate
				int colCount = 0;
				Node col = row;
				do {
					colCount++;
					assertFalse("Node count in a row exceeded 4.", colCount > 4);
					col = col.R;
				} while (col != row);
				assertEquals("Node count in a row is not 4.", 4, colCount);
				
				row = row.D;
			} while (row != head.first);
			assertEquals("Node count in a column is not as expected.", boardSize, rowCount);
			
			head = head.next;
		}
		
		assertEquals("Head count is not correct.", expectedHeadCount, headCount);
		assertEquals("Last head doesn't point to main.", main, head);
	}
	
	@Test
	public void testBuilder() {
		for (int i = 2; i <= 4; i++) {
			testPattern(i, i);
		}
	}
	
	@Test
	@Ignore
	public void testRandomPerformance() {
		final int total = 1000;
		
		long time = 0;
		int failureCount = 0;
		Board mockBoard = new Board();
		for (int i = 0; i < total; i++) {
			try {
				long t0 = System.nanoTime();
				Randomizer.generate(3, 3, mockBoard);
				long span = System.nanoTime() - t0;
				time += span;
			}
			catch (Exception e) {
				logger.error("Encountered error!", e);
				failureCount++;
			}
		}
		
		logger.debug("Total time: " + ((float)time / 1000000) + " ms");
		float successRate = (float) (total - failureCount) / total;
		logger.debug("Success rate: " + successRate * 100 + "%");
	}
	
	@Test
	@Ignore
	public void testHead() {
		Head head = new Head();
		
		for (int i = 0; i < 127; i++) {
			head.increaseCount();
			logger.debug(head.getCount() + "");
			List<Node> candidates = new ArrayList<Node>(head.getCount());
			candidates.isEmpty();
		}
		
		for (int i = 0; i < 128; i++) {
			head.decreaseCount();
			logger.debug(head.getCount() + "");
		}
	}
	
	/**
	 * This test ensures that DLX builds 4 linked constrained nodes correctly in a row
	 */
	@Test
	public void testNodes() {
		final int conCount = 4;
		final byte row = 0;
		final byte col = 0;
		final byte val = 0;
		
		// create 4 nodes for 4 constraints and link them together
		Node[] nodes = new Node[conCount];
		nodes[0] = new Node(row, col, val);
		for (int i = 1; i < conCount; i++) {
			nodes[i] = new Node(row, col, val);
			nodes[i].L = nodes[i - 1];
			nodes[i - 1].R = nodes[i];
		}
		nodes[0].L = nodes[conCount - 1];
		nodes[conCount - 1].R = nodes[0];
		
		// get ordered addresses of 4 nodes
		String[] addresses = new String[conCount];
		for (int i = 0; i < conCount; i++) {
			addresses[i] = nodes[i].address();
		}
		
		// make sure that all 4 pointers are circular linked list
		
		Node pointer = nodes[0];
		int counter = 0;
		do {
			assertEquals("Pointer address is not the same order with the array.", addresses[counter++], pointer.address());
			pointer = pointer.R;
		} while (pointer != nodes[0]);
		assertEquals("Last pointer doesn't point to address of the first node.", addresses[0], pointer.address());
	}
	
	@Test
	public void testPointerChanging() {
		byte b1 = 1, b2 = 2, b3 = 3;
		Node p1 = new Node(b1,b1,b1);
		Node p2 = new Node(b2,b2,b2);
		Node p3 = new Node(b3,b3,b3);
		
		printPointers(p1, p2, p3);
		
		// link 3 pointers as circular linked list
		// ... <-> p1 <-> p2 <-> p3 <-> ...
		p1.R = p2;
		p2.L = p1;
		p2.R = p3;
		p3.L = p2;
		p3.R = p1;
		p1.L = p3;
		
		printPointers(p1, p2, p3);
		
		// detect p1 and p3 using p2
		// then directly link p1 and p3
		Node back = p2.L;
		Node next = p2.R;
		back.R = next;
		next.L = back;
		
		printPointers(p1, p2, p3);
		
		// try to backtrack, relink p1 and p3 to p2
		back = p2.L;
		next = p2.R;
		back.R = p2;
		next.L = p2;
		
		printPointers(p1, p2, p3);
	}
	
	public void printPointers(Node... pointers) {
		logger.debug("====================================");
		int counter = 1;
		for (Node pointer : pointers) {
			String id = "p" + counter;
			logger.debug(id + ".L : " + pointer.L.address());
			logger.debug(id + "   : " + pointer.address());
			logger.debug(id + ".R : " + pointer.R.address());
			counter++;
		}
	}
}
