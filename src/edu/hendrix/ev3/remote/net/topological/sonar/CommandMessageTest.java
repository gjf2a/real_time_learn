package edu.hendrix.ev3.remote.net.topological.sonar;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommandMessageTest {

	@Test
	public void test() {
		CommandMessage start = CommandMessage.makeStartup(100);
		CommandMessage rebuilt = CommandMessage.fromBytes(start.toBytes());
		assertEquals(start, rebuilt);
	}

}
