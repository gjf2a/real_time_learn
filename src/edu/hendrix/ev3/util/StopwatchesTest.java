package edu.hendrix.ev3.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class StopwatchesTest {
	
	Stopwatches<Opt> s;
	
	@Before
	public void setup() {
		s = new Stopwatches<>(Opt.class);
	}
	
	enum Opt {TRAIN, AUTO};

	void assertBetween(long value, long lo, long hi) {
		System.out.printf("Checking %d in [%d,%d]\n", value, lo, hi);
		assertTrue(value >= lo && value <= hi);
	}
	
	@Test
	public void test1() throws InterruptedException {
		System.out.println("test1");
		s.startEpisodeFor(Opt.TRAIN);
		Thread.sleep(50);
		s.startEpisodeFor(Opt.AUTO);
		Thread.sleep(50);
		s.endEpisodeFor(Opt.TRAIN);
		Thread.sleep(50);
		s.endEpisodeFor(Opt.AUTO);
		
		assertEquals(2, s.allLabels().size());
		assertTrue(s.hasLabel(Opt.TRAIN));
		assertTrue(s.hasLabel(Opt.AUTO));
		assertBetween(s.getValueFor(Opt.AUTO, 0), 100, 110);
		assertBetween(s.getValueFor(Opt.TRAIN, 0), 100, 110);
	}
	
	@Test
	public void test2() throws InterruptedException {
		test1();
		System.out.println("test2");
		s.startEpisodeFor(Opt.TRAIN);
		Thread.sleep(50);
		s.endEpisodeFor(Opt.TRAIN);
		assertBetween(s.getValueFor(Opt.TRAIN, 1), 50, 60);
	}
	
	@Test
	public void test3() throws InterruptedException {
		System.out.println("test3");
		s.startEpisodeFor(Opt.TRAIN);
		Thread.sleep(50);
		s.startEpisodeFor(Opt.AUTO);
		Thread.sleep(50);
		s.startEpisodeFor(Opt.TRAIN);
		Thread.sleep(50);
		s.endEpisodeFor(Opt.AUTO);
		s.endEpisodeFor(Opt.TRAIN);
		
		assertEquals(2, s.allLabels().size());
		assertTrue(s.hasLabel(Opt.TRAIN));
		assertTrue(s.hasLabel(Opt.AUTO));
		assertBetween(s.getValueFor(Opt.AUTO, 0), 100, 110);
		assertBetween(s.getValueFor(Opt.TRAIN, 0), 100, 110);
		assertBetween(s.getValueFor(Opt.TRAIN, 1), 50, 60);
	}
}
