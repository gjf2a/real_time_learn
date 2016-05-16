package edu.hendrix.ev3.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CircularListTest {

	@Test
	public void test() {
		CircularList<Integer> list = new CircularList<>();
		list.forward();
		list.backward();
		for (int i = 0; i < 10; i++) {
			list.add(i);
			assertEquals(list.size() - 1, list.getCurrentIndex());
			assertEquals(new Integer(i), list.getCurrentItem());
		}
		
		for (int i = 0; i < list.size(); i++) {
			list.forward();
			assertEquals(i, list.getCurrentIndex());
		}
		
		while (list.getCurrentIndex() > 0) {
			list.backward();
		}
		
		list.backward();
		assertEquals(list.size() - 1, list.getCurrentIndex());
	}

}
