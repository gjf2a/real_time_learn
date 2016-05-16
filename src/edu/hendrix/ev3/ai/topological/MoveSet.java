package edu.hendrix.ev3.ai.topological;

import java.util.EnumSet;
import java.util.Iterator;

import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Util;

public class MoveSet implements Iterable<Move> {
	private EnumSet<Move> moves;
	
	public MoveSet() {
		Util.assertState(Move.values().length <= Integer.BYTES * 8, "Too many Move enum values");
		moves = EnumSet.noneOf(Move.class);
	}
	
	public MoveSet(MoveSet other) {
		this();
		for (Move move: this) {
			other.add(move);
		}
	}
	
	public Move[] toArray() {
		Move[] array = new Move[moves.size()];
		int i = 0;
		for (Move move: this) {
			array[i++] = move;
		}
		return array;
	}

	@Override
	public Iterator<Move> iterator() {
		return moves.iterator();
	}
	
	public void add(Move m) {moves.add(m);}
	
	public boolean contains(Move m) {return moves.contains(m);}
	
	public int toInt() {
		int result = 0;
		int bit = 1;
		for (Move move: Move.values()) {
			if (moves.contains(move)) {
				result |= bit;
			}
			bit <<= 1;
		}
		return result;
	}
	
	public static MoveSet fromInt(int encoded) {
		MoveSet result = new MoveSet();
		int ordinal = 0;
		while (encoded > 0) {
			if ((encoded & 0x1) == 1) {
				result.add(Move.values()[ordinal]);
			}
			encoded >>= 1;
			ordinal += 1;
		}
		return result;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof MoveSet) {
			MoveSet that = (MoveSet)other;
			return this.moves.equals(that.moves);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Move move: this) {
			result.append(move);
			result.append(',');
		}
		if (result.length() > 0) {
			result.deleteCharAt(result.length() - 1);
		}
		return result.toString();
	}
}
