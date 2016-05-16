package edu.hendrix.ev3.ai.topological.sonar;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.hendrix.ev3.remote.sonar.SonarPosition;

public class ClusterableSonarStateTest {
	ClusterableSonarState state1, state2;
	
	@Before
	public void setup() {
		state1 = new ClusterableSonarState();
		state1.setReading(SonarPosition.LEFT, 1.0f);
		state1.setReading(SonarPosition.CENTER, 1.0f);
		state1.setReading(SonarPosition.RIGHT, 1.0f);
		
		state2 = new ClusterableSonarState();
		state2.setReading(SonarPosition.LEFT, 3.0f);
		state2.setReading(SonarPosition.CENTER, 4.0f);
		state2.setReading(SonarPosition.RIGHT, 5.0f);
	}
	
	@Test
	public void testString() {
		assertEquals("LEFT:1.0;CENTER:1.0;RIGHT:1.0", state1.toString());
		assertEquals("LEFT:3.0;CENTER:4.0;RIGHT:5.0", state2.toString());
	}

	@Test
	public void testGoal1() {
		ClusterableSonarState goal1 = new ClusterableSonarState();
		goal1.setReading(SonarPosition.LEFT, 2.5f);
		goal1.setReading(SonarPosition.CENTER, 3.25f);
		goal1.setReading(SonarPosition.RIGHT, 4.0f);
		
		assertEquals(goal1, state1.weightedCentroidWith(state2, 1, 3));
	}

}
