package edu.hendrix.ev3.imgproc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BitImageTest {
	
	BitImage bimg;
	
	@Before
	public void setup() {
		bimg = new BitImage(4, 5);
		for (int i = 1; i < bimg.getWidth();i++) {
			bimg.set(i, i);
		}
		System.out.println(bimg);
	}
	
	@Test
	public void testCopy() {
		BitImage copy = new BitImage(bimg);
		assertEquals(bimg, copy);
	}
}
