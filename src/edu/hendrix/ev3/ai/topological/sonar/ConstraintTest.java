package edu.hendrix.ev3.ai.topological.sonar;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConstraintTest {

	@Test
	public void testTighten() {
		Constraint c = new Constraint();
		assertTrue(c.test(Float.MAX_VALUE));
		assertTrue(c.test(Float.MIN_VALUE));
		
		c.tighten(new Constraint(1, 5));
		assertFalse(c.test(Float.MAX_VALUE));
		assertFalse(c.test(Float.MIN_VALUE));
		assertFalse(c.test(0));
		assertFalse(c.test(6));
		assertTrue(c.test(1));
		assertTrue(c.test(3));
		assertTrue(c.test(5));
		
		c.tighten(new Constraint(3, 7));
		assertFalse(c.test(2));
		assertTrue(c.test(3));
		assertTrue(c.test(4));
		assertTrue(c.test(5));
		assertFalse(c.test(6));
		assertFalse(c.test(7));
	}

	@Test
	public void testLoosen() {
		Constraint c = new Constraint(5);
		assertFalse(c.test(4));
		assertTrue(c.test(5));
		assertFalse(c.test(6));
		
		c.loosen(new Constraint(3));
		assertFalse(c.test(2));
		assertTrue(c.test(3));
		assertTrue(c.test(4));
		assertTrue(c.test(5));
		assertFalse(c.test(6));
	}
}
