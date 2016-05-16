package edu.hendrix.ev3.util;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class FixedSizeArrayTest {
	
	FixedSizeArray<String> target;
	final static int CAPACITY = 5;
	
	@Before
	public void setup() {
		target = FixedSizeArray.makeImmutableType(CAPACITY);
	}

	@Test
	public void test1() {
		assertEquals(0, target.size());
		assertEquals(CAPACITY, target.capacity());
		target.add("a");
		target.add("b");
		assertEquals(2, target.size());
		assertEquals(CAPACITY, target.capacity());
		assertEquals("a", target.get(0));
		assertEquals("b", target.get(1));
		assertEquals(0, target.getLowestInUse());
		assertEquals(1, target.getHighestInUse());
		assertEquals(2, target.getLowestAvailable());
	}

	@Test
	public void test2() {
		test1();
		assertEquals("a", target.remove(0));
		assertTrue(target.isAvailable(0));
		assertEquals(1, target.size());
		target.add("c");
		assertEquals("c", target.get(0));
		assertEquals("b", target.get(1));
		assertEquals(0, target.getLowestInUse());
		assertEquals(1, target.getHighestInUse());
		assertEquals(2, target.getLowestAvailable());
	}
	
	@Test
	public void test3() {
		test1();
		target.add("c");
		assertEquals(3, target.size());
		assertEquals("c", target.get(2));
		
		ArrayList<Integer> inUse = getInUse();
		assertEquals(3, inUse.size());
		for (int i = 0; i < inUse.size(); i++) {
			assertTrue(i == inUse.get(i));
		}
		
		ArrayList<Integer> unused = getUnused();
		assertEquals(2, unused.size());
		assertTrue(3 == unused.get(0));
		assertTrue(4 == unused.get(1));
		
		ArrayList<String> values = target.values();
		assertEquals(target.size(), values.size());
		assertEquals("a", values.get(0));
		assertEquals("b", values.get(1));
		assertEquals("c", values.get(2));
	}
	
	@Test
	public void test4() {
		test3();
		assertEquals("b", target.remove(1));
		assertTrue(target.isAvailable(1));
		
		ArrayList<Integer> inUse = getInUse();
		assertEquals(2, inUse.size());
		assertTrue(0 == inUse.get(0));
		assertTrue(2 == inUse.get(1));
		
		ArrayList<Integer> unused = getUnused();
		assertEquals(3, unused.size());
		assertTrue(1 == unused.get(0));
		assertTrue(3 == unused.get(1));
		assertTrue(4 == unused.get(2));
		
		ArrayList<String> values = target.values();
		assertEquals(target.size(), values.size());
		assertEquals("a", values.get(0));
		assertEquals("c", values.get(1));
		
		assertEquals(0, target.getLowestInUse());
		assertEquals(2, target.getHighestInUse());
		assertEquals(1, target.getLowestAvailable());
	}
	
	ArrayList<Integer> getInUse() {
		ArrayList<Integer> inUse = new ArrayList<>();
		for (int i = target.getLowestInUse(); i < target.capacity(); i = target.nextInUse(i)) {
			inUse.add(i);
		}
		return inUse;
	}
	
	ArrayList<Integer> getUnused() {
		ArrayList<Integer> unused = new ArrayList<>();
		for (int i = target.getLowestAvailable(); i < target.capacity(); i = target.nextAvailable(i)) {
			unused.add(i);
		}
		return unused;
	}
}
