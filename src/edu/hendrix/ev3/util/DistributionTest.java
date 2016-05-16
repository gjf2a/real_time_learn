package edu.hendrix.ev3.util;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class DistributionTest {
	
	public static final int NUM_TESTS = 10000;

	@Test
	public void simpleTest() {
		Distribution dist = new Distribution(1,2,3,4);
		assertEquals(4, dist.numOutcomes());
		Distribution dist2 = new Distribution(dist.toString());
		assertEquals(dist, dist2);
		
		int[] counts = makeHistogram(NUM_TESTS, dist);
		testPortion(0.1, counts[0]);
		testPortion(0.2, counts[1]);
		testPortion(0.3, counts[2]);
		testPortion(0.4, counts[3]);
	}

	public void testPortion(double target, int count) {
		assertEquals(target, (double)count / NUM_TESTS, 0.01);
	}
	
	@Test
	public void normalTest() {
		Distribution dist = Distribution.makeNormal(10000, 0, 2);
		int[] counts = makeHistogram(NUM_TESTS, dist);
		assertNormalHistogram(counts, 0, 2);
	}
	
	public int[] makeHistogram(int reps, Distribution d) {
		int[] counts = new int[d.numOutcomes()];
		for (int i = 0; i < reps; i++) {
			counts[d.pick()]++;
		}
		return counts;
	}
	
	public void assertNormalHistogram(int[] histogram, double mu, double sigma) {
		assertCorrectPortion(histogram, normalSubBounds(histogram, 1, mu, sigma), 0.68);
		assertCorrectPortion(histogram, normalSubBounds(histogram, 2, mu, sigma), 0.95);
	}
	
	public Duple<Integer,Integer> normalSubBounds(int[] histogram, int numStdDevs, double mu, double sigma) {
		double muIndex = Distribution.normalMidpointIndex(histogram.length);
		return new Duple<>((int)(1 + muIndex - sigma * numStdDevs), (int)(muIndex + sigma * numStdDevs));
	}
	
	public void assertCorrectPortion(int[] histogram, Duple<Integer,Integer> bounds, double target) {
		int total = Arrays.stream(histogram).sum();
		int[] sub = Arrays.copyOfRange(histogram, bounds.getFirst(), bounds.getSecond() + 1);
		int subtotal = Arrays.stream(sub).sum();
		assertEquals(target, (double)subtotal/total, 0.02);
	}
}
