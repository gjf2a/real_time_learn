package edu.hendrix.ev3.ai.topological;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.hendrix.ev3.remote.Move;

public class PathTest {
	
	Path p1, p2, p3;
	
	@Before
	public void setup() {
		p1 = new Path(0);
		p2 = p1.createSuccessor(Move.FORWARD, 1, 5.0/16);
		p3 = p2.createSuccessor(Move.FORWARD, 2, 5.0/15);
	}
	
	@Test
	public void copyTest() {
		assertEquals(p2, new Path(p2));
	}

	@Test
	public void test() {
		assertEquals(1.0, p1.getProbability(), 0.0001);
		assertEquals(0, p1.getNumMoves());
		assertEquals("0", p1.toString());
		assertEquals(5.0/16, p2.getProbability(), 0.0001);
		assertEquals(1, p2.getNumMoves());
		assertEquals("0;FORWARD:1", p2.toString());
		assertEquals(5.0/16 * 5.0/15, p3.getProbability(), 0.0001);
		assertEquals(2, p3.getNumMoves());
		assertEquals("0;FORWARD:1;FORWARD:2", p3.toString());
	}
}
