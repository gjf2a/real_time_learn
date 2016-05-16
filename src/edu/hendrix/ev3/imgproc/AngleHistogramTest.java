package edu.hendrix.ev3.imgproc;

import static org.junit.Assert.*;

import org.junit.Test;

public class AngleHistogramTest {

	@Test
	public void test() {
		AngleHistogram hist = new AngleHistogram(4);
		assertEquals(4, hist.size());
		assertEquals(Math.PI / 2, hist.getBucketSize(), 0.001);
		
		hist.bump(Math.PI);
		hist.bump(Math.PI / 3);
		hist.bump(2 * Math.PI / 3);
		hist.bump(Math.PI / 4);
		hist.bump(3 * Math.PI / 4);
		hist.bump(4 * Math.PI / 5);
		hist.bump(7 * Math.PI / 4);
		hist.bump(9 * Math.PI / 5);
		hist.bump(11 * Math.PI / 6);
		hist.bump(-Math.PI / 7);
		
		assertEquals(2, hist.getCountFor(0));
		assertEquals(3, hist.getCountFor(1));
		assertEquals(1, hist.getCountFor(2));
		assertEquals(4, hist.getCountFor(3));
	}

}
