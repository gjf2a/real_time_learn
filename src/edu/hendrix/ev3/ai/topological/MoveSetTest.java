package edu.hendrix.ev3.ai.topological;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.hendrix.ev3.remote.Move;

public class MoveSetTest {

	@Test
	public void test() {
		MoveSet moves = new MoveSet();
		assertEquals(0, moves.toInt());
		moves.add(Move.FORWARD);
		assertEquals(1, moves.toInt());
		moves.add(Move.RIGHT);
		assertEquals(5, moves.toInt());
		assertEquals(moves, MoveSet.fromInt(moves.toInt()));
		
		moves.add(Move.SPIN_LEFT);
		assertEquals(moves, MoveSet.fromInt(moves.toInt()));
		moves.add(Move.BACKWARD);
		assertEquals(moves, MoveSet.fromInt(moves.toInt()));
		moves.add(Move.LEFT);
		assertEquals(moves, MoveSet.fromInt(moves.toInt()));
	}

}
