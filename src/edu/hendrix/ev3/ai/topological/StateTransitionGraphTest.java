package edu.hendrix.ev3.ai.topological;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import edu.hendrix.ev3.remote.Move;

public class StateTransitionGraphTest {
	
	StateTransitionGraph graph1;
	
	@Before
	public void setup() {
		graph1 = new StateTransitionGraph();
		addNTransitions(graph1, 10, 0, Move.FORWARD, 0);
		addNTransitions(graph1, 5, 0, Move.FORWARD, 1);
		addNTransitions(graph1, 1, 0, Move.FORWARD, 2);
		addNTransitions(graph1, 10, 1, Move.FORWARD, 0);
		addNTransitions(graph1, 5, 1, Move.FORWARD, 2);
		addNTransitions(graph1, 1, 2, Move.FORWARD, 2);
	}
	
	@Test
	public void testNotThere() {
		assertEquals(0, graph1.getCountFrom(-1));
		assertEquals(0, graph1.getCountForMoveFrom(-1, Move.FORWARD));
		assertEquals(0, graph1.getCountToSuccessor(-1, Move.FORWARD, 0));
		assertEquals(0, graph1.getCountToSuccessor(1, Move.FORWARD, -1));
		assertEquals(0, graph1.getCountForMoveFrom(0, Move.SPIN_LEFT));
		assertEquals(0, graph1.getCountToSuccessor(0, Move.SPIN_LEFT, 1));
	}
	
	void addNTransitions(StateTransitionGraph graph, int n, int start, Move transition, int end) {
		if (!graph.hasNode(start)) graph.addNode(start);
		if (!graph.hasNode(end)) graph.addNode(end);
		for (int i = 0; i < n; i++) {
			graph.addTransition(start, transition, end);
		}
	}

	@Test
	public void bestPathTest1() {
		PathsFrom paths = graph1.bestPathsFrom(0);
		assertEquals(0, paths.getSource());
		assertEquals(2, paths.getNumDestinations());
		assertEquals(5.0/16, paths.pathTo(1).getProbability(), 0.00001);
		assertEquals(1, paths.pathTo(1).getNumMoves());
		assertEquals("0;FORWARD:1", paths.pathTo(1).toString());
		assertEquals(5.0/16 * 5.0/15, paths.pathTo(2).getProbability(), 0.00001);
		assertEquals(2, paths.pathTo(2).getNumMoves());
		assertEquals("0;FORWARD:1;FORWARD:2", paths.pathTo(2).toString());
	}
	
	@Test
	public void mergeTest() {
		graph1.assertInvariant();
		graph1.purgeAndMerge(2, 1);
		graph1.assertInvariant();
		HashMap<Integer,Double> successors0 = graph1.successorsTo(0, Move.FORWARD);
		HashMap<Integer,Double> successors1 = graph1.successorsTo(1, Move.FORWARD);
		assertEquals(6.0/16, successors0.get(1), 0.00001);
		assertEquals(10.0/16, successors1.get(0), 0.00001);
		for (int node: graph1.getAllNodes()) {
			for (int next: graph1.getSuccessorIds(node, Move.FORWARD)) {
				System.out.printf("node: %d next: %d\n", node, next);
			}
		}
	}
	
	@Test
	public void stringTest() {
		assertEquals("{{0{{FORWARD{{0;0;10}{0;1;5}{0;2;1}}}}}{1{{FORWARD{{1;0;10}{1;2;5}}}}}{2{{FORWARD{{2;2;1}}}}}}{{0{0;1;}}{1{0;}}{2{0;1;2;}}}", graph1.toString());
		StateTransitionGraph rebuilt = new StateTransitionGraph(graph1.toString());
		assertEquals(graph1, rebuilt);
	}
	
	@Test
	public void stringTest2() {
		graph1.addNode(3);
		assertEquals("{{0{{FORWARD{{0;0;10}{0;1;5}{0;2;1}}}}}{1{{FORWARD{{1;0;10}{1;2;5}}}}}{2{{FORWARD{{2;2;1}}}}}{3{}}}{{0{0;1;}}{1{0;}}{2{0;1;2;}}{3{}}}", graph1.toString());
		StateTransitionGraph rebuilt = new StateTransitionGraph(graph1.toString());
		assertEquals(graph1, rebuilt);		
	}
}
