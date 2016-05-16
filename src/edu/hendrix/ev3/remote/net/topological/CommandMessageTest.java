package edu.hendrix.ev3.remote.net.topological;

import static org.junit.Assert.*;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

public class CommandMessageTest {
	CommandMessage msg;
	LocalDateTime current;
	
	@Before
	public void setup() {
		current = LocalDateTime.now();
		msg = new CommandMessage(Command.NEW_BSOC, (byte)1, 100, 1, current);
	}

	@Test
	public void test() {
		assertEquals("Command:NEW_BSOC;Tag:1;Target:100;Shrink:1" + ";Stamp:" + current.toString(), msg.toString());
		byte[] bytes = msg.toBytes();
		assertEquals(CommandMessage.NUM_BYTES, bytes.length);
		assertEquals(msg, CommandMessage.fromBytes(bytes));
	}

}
