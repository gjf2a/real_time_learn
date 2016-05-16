package edu.hendrix.ev3.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.Test;

public class StableMatchPrefsTest {
	
	public static int distance(char c, int i) {
		return Math.abs(c - 'a' + 1 - i);
	}

	@Test
	public void test1() {
		ArrayList<Character> chars = new ArrayList<>();
		for (char c: new char[]{'a', 'c', 'd', 'e', 'g', 'm', 'z'}) {
			chars.add(c);
		}
		ArrayList<Integer> ints = new ArrayList<>();
		for (int i: new int[]{2, 3, 5, 6, 8, 10, 12, 27}) {
			ints.add(i);
		}
		
		LinkedHashMap<Character,Integer> matches = StableMatchPrefs.makeStableMatches(chars, ints, StableMatchPrefsTest::distance);
		assertTrue(2 == matches.get('a'));
		assertTrue(3 == matches.get('c'));
		assertTrue(8 == matches.get('d'));
		assertTrue(5 == matches.get('e'));
		assertTrue(6 == matches.get('g'));
		assertTrue(12 == matches.get('m'));
		assertTrue(27 == matches.get('z'));
	}

}
