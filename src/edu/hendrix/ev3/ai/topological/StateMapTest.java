package edu.hendrix.ev3.ai.topological;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.hendrix.ev3.ai.bsoc.BSOCTestee;
import edu.hendrix.ev3.ai.topological.sonar.ClusterableSonarState;
import edu.hendrix.ev3.ai.topological.sonar.visualize.CmdLineVisualizer;
import edu.hendrix.ev3.remote.Move;

public class StateMapTest {

	final static int NUM_NODES = 3;
	
	StateMap<BSOCTestee> map;
	
	@Before
	public void setup() {
		System.out.println("setup");
		map = new StateMap<>(NUM_NODES, BSOCTestee::distance);
		int currentState = 0;
		for (int i = 0; i < NUM_NODES * 2; i++) {
			int numOutgoing = map.numCounts();
			currentState = map.addTransition(currentState, Move.FORWARD, new BSOCTestee(i));
			assertEquals(numOutgoing + 1, map.numCounts());
		}		
	}
	
	@Test
	public void basicTest1() {
		System.out.println("basicTest1");
		assertEquals("{{3{}}\n{{{0}{2}{0}}{{1}{2}{2}}{{2}{2}{4}}}\n{{0;1;4}{1;2;4}{0;2;8}}}\n{{{0{{FORWARD{{0;0;2}{0;1;1}}}}}{1{{FORWARD{{1;1;1}{1;2;1}}}}}{2{{FORWARD{{2;2;1}}}}}}{{0{0;}}{1{0;1;}}{2{1;2;}}}}", map.toString());
		System.out.println();
	}
	
	@Test
	public void basicTest2() {
		System.out.println("basicTest2");
		int currentState = 0;
		for (int i = 0; i < NUM_NODES; i++) {
			currentState = map.addTransition(currentState, Move.SPIN_LEFT, new BSOCTestee(i*2));
		}
		assertEquals("{{3{}}\n{{{0}{3}{0}}{{1}{3}{2}}{{2}{3}{4}}}\n{{0;1;6}{1;2;6}{0;2;12}}}\n{{{0{{FORWARD{{0;0;2}{0;1;1}}}{SPIN_LEFT{{0;0;1}{0;1;1}}}}}{1{{FORWARD{{1;1;1}{1;2;1}}}{SPIN_LEFT{{1;2;1}}}}}{2{{FORWARD{{2;2;1}}}}}}{{0{0;}}{1{0;1;}}{2{1;2;}}}}", map.toString());
		System.out.println();
	}
	
	@Test
	public void basicTest3() {
		System.out.println("basicTest3");
		int currentState = 1;
		currentState = map.addTransition(currentState, Move.FORWARD, new BSOCTestee(100));
		assertEquals("{{3{}}\n{{{0}{4}{1}}{{1}{1}{100}}{{2}{2}{4}}}\n{{0;2;12}{1;2;192}{0;1;396}}}\n{{{0{{FORWARD{{0;0;4}{0;1;1}{0;2;1}}}}}{1{}}{2{{FORWARD{{2;2;1}}}}}}{{0{0;}}{1{0;}}{2{0;2;}}}}", map.toString());
		System.out.println();
	}
	
	@Test
	public void simplifiedTranscriptTest1() {
		System.out.println("simplifiedTranscriptTest1");
		StateMap<ClusterableSonarState> result = ClusterableSonarState.makeStateMap(5);
		result.addTransition(0, Move.STOP, new ClusterableSonarState("LEFT:0.67200005;CENTER:0.42000002;RIGHT:0.869"));
		result.addTransition(0, Move.FORWARD, new ClusterableSonarState("LEFT:0.66800004;CENTER:0.416;RIGHT:0.869"));
		result.addTransition(0, Move.FORWARD, new ClusterableSonarState("LEFT:0.71800005;CENTER:0.38500002;RIGHT:0.384"));
		result.addTransition(0, Move.FORWARD, new ClusterableSonarState("LEFT:0.87100005;CENTER:0.36100003;RIGHT:0.35700002"));
		result.addTransition(0, Move.FORWARD, new ClusterableSonarState("LEFT:0.37;CENTER:0.35300002;RIGHT:0.321"));
		result.addTransition(0, Move.FORWARD, new ClusterableSonarState("LEFT:0.595;CENTER:0.321;RIGHT:0.326"));
		result.assertInvariant();
		System.out.println();
	}
	
	@Ignore
	@Test
	public void transcriptTest1() throws FileNotFoundException {
		transcriptTest("SonarBot.log_1");
	}
	
	@Ignore
	@Test
	public void transcriptTest2() throws FileNotFoundException {
		transcriptTest("SonarBot.log_2");
	}
	
	public void transcriptTest(String logFile) throws FileNotFoundException {
		System.out.println("testing " + logFile);
		StateMap<ClusterableSonarState> fromRobot = CmdLineVisualizer.makeMapFrom(logFile);
		fromRobot.assertInvariant();		
	}
}
