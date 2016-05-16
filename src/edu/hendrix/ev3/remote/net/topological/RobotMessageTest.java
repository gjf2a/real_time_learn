package edu.hendrix.ev3.remote.net.topological;

import static org.junit.Assert.*;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

public class RobotMessageTest {
	
	RobotMessage msg;
	
	@Before
	public void setup() {
		msg = new RobotMessage((byte)1, false, false);
	}

	@Test
	public void test() {
		assertEquals(1, msg.getTag());
		assertFalse(msg.hasPhoto());
	}
	
	@Test
	public void testArchives() {
		LocalDateTime zero = LocalDateTime.of(2016, 1, 24, 2, 27);
		LocalDateTime one = LocalDateTime.of(2016, 1, 25, 3, 29);
		msg = new RobotMessage((byte)1, true, zero, one);
		RobotMessage dup = new RobotMessage(msg.toBytes());
		assertTrue(dup.hasArchives());
		assertFalse(dup.hasPhoto());
		assertEquals(2, dup.numArchives());
		assertEquals(zero, dup.getArchive(0));
		assertEquals(one, dup.getArchive(1));
	}
}
