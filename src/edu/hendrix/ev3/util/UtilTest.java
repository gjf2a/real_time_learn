package edu.hendrix.ev3.util;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class UtilTest {

	@Test
	public void debraceTest() {
		String testInput = "{one;{two;three;four}{five{six;seven}eight{nine;ten}}}";
		ArrayList<String> debraced = Util.debrace(testInput);
		for (String s: debraced) {System.out.println(s);}
		assertEquals(1, debraced.size());
		assertEquals(testInput.substring(1, testInput.length() - 1), debraced.get(0));
		
		debraced = Util.debrace(debraced.get(0));
		for (String s: debraced) {System.out.println(s);}
		assertEquals(3, debraced.size());
		assertEquals("one;", debraced.get(0));
		assertEquals("two;three;four", debraced.get(1));
		assertEquals("five{six;seven}eight{nine;ten}", debraced.get(2));
		
		debraced = Util.debrace(debraced.get(2));
		for (String s: debraced) {System.out.println(s);}
		assertEquals(4, debraced.size());
		assertEquals("five", debraced.get(0));
		assertEquals("six;seven", debraced.get(1));
		assertEquals("eight", debraced.get(2));
		assertEquals("nine;ten", debraced.get(3));
	}
	
	@Test
	public void debraceTest2() {
		String testInput = "{one;two}\n{three;four}";
		ArrayList<String> debraced = Util.debrace(testInput);
		assertEquals(2, debraced.size());
		assertEquals("one;two", debraced.get(0));
		assertEquals("three;four", debraced.get(1));
	}
	
	@Test
	public void debraceTest3() {
		String testInput = "{one two}\n{three four}";
		ArrayList<String> debraced = Util.debrace(testInput);
		assertEquals(2, debraced.size());
		assertEquals("one two", debraced.get(0));
		assertEquals("three four", debraced.get(1));
	}
	
	@Test
	public void boolTest() {
		assertTrue(Util.byte2bool(Util.bool2byte(true)));
		assertFalse(Util.byte2bool(Util.bool2byte(false)));
	}
	
	@Test
	public void logFloorTest() {
		assertEquals(0, Util.log2Floor(1));
		assertEquals(1, Util.log2Floor(2));
		assertEquals(1, Util.log2Floor(3));
		assertEquals(2, Util.log2Floor(4));
		assertEquals(2, Util.log2Floor(5));
		assertEquals(2, Util.log2Floor(7));
		assertEquals(3, Util.log2Floor(8));
		assertEquals(3, Util.log2Floor(9));
		assertEquals(3, Util.log2Floor(15));
		assertEquals(4, Util.log2Floor(16));
	}	
	
	@Test
	public void powTest() {
		assertEquals(1,    Util.pow(3, 0));
		assertEquals(3,    Util.pow(3, 1));
		assertEquals(9,    Util.pow(3, 2));
		assertEquals(27,   Util.pow(3, 3));
		assertEquals(81,   Util.pow(3, 4));
		assertEquals(243,  Util.pow(3, 5));
		assertEquals(729,  Util.pow(3, 6));
		assertEquals(2187, Util.pow(3, 7));
		assertEquals(6561, Util.pow(3, 8));
	}
	
	@Test
	public void bigPowTest() {
		assertEquals(2147483648L, Util.pow(2, 31));
		assertEquals(4294967296L, Util.pow(2, 32));
	}
}
