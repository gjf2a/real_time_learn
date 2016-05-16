package edu.hendrix.ev3.remote.net.imitation;

import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Duple;

public class CommandMessage extends UserMessage {
	private Move move;
	
	public static final int NUM_BYTES = 3;
	
	public CommandMessage(byte[] bytes) {
		super(Mode.values()[bytes[0]], bytes[1]);
		this.move = Move.values()[bytes[2]];
	}
	
	public CommandMessage(Mode mode, Move move) {
		super(mode);
		this.move = move;
	}

	@Override
	public byte[] toBytes() {
		byte[] result = new byte[UserMessage.NUM_BYTES];
		result[0] = (byte)getMode().ordinal();
		result[1] = getTag();
		result[2] = (byte)move.ordinal();
		return result;
	}
	
	public Move getMove() {return move;}
	
	public Duple<Move,Mode> unpack() {return new Duple<>(move,getMode());}
	
	@Override
	public String toString() {return String.format("CommandMessage: Move: %s Mode: %s Tag: %d", move, getMode(), getTag());}
}
