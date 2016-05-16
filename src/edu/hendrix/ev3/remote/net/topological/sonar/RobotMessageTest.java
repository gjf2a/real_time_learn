package edu.hendrix.ev3.remote.net.topological.sonar;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.hendrix.ev3.ai.topological.sonar.ClusterableSonarState;
import edu.hendrix.ev3.remote.sonar.SonarPosition;

public class RobotMessageTest {

	@Test
	public void test() {
		ClusterableSonarState sonar = new ClusterableSonarState();
		sonar.setReading(SonarPosition.LEFT, 0.5f);
		sonar.setReading(SonarPosition.CENTER, 1.0f);
		sonar.setReading(SonarPosition.RIGHT, 1.5f);
		RobotMessage msg = new RobotMessage((byte)1, true, sonar);
		assertEquals("Tag:1;Initialized:Yes;Sonar:{LEFT:0.5;CENTER:1.0;RIGHT:1.5}", msg.toString());
		assertEquals(msg, new RobotMessage(msg.toBytes()));
	}

}
