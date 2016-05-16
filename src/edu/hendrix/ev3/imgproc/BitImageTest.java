package edu.hendrix.ev3.imgproc;

import static org.junit.Assert.*;

import org.junit.Test;

public class BitImageTest {

	@Test
	public void test() {
		BitImage bimg = new BitImage(4, 5);
		for (int i = 1; i < bimg.getWidth();i++) {
			bimg.set(i, i);
		}
		System.out.println(bimg);
		BitImage sub = bimg.getSubimage(1, 1, 3, 3);
		System.out.println(sub);
		assertEquals(3, sub.getWidth());
		assertEquals(3, sub.getHeight());
		for (int x = 0; x < sub.getWidth(); x++) {
			for (int y = 0; y < sub.getHeight(); y++) {
				if (x == y) {
					assertTrue(sub.isSet(x, y));
				} else {
					assertFalse(sub.isSet(x, y));
				}
			}
		}
	}

}
