package edu.hendrix.ev3.remote.net.imitation;

import static org.junit.Assert.*;

import org.junit.Test;

public class StartupMessageTest {

	@Test
	public void test() {
		StartupMessage msg = new StartupMessage(100, 10);
		assertEquals(msg, StartupMessage.fromBytes(msg.toBytes()));
	}

}
