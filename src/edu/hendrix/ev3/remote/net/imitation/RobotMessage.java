package edu.hendrix.ev3.remote.net.imitation;

import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.TaggedMessage;

// What these messages mean:
//
// currentMode: The robot's current Mode
// trainedMove: Move given by the user for the most recent image
// - If in Mode.AUTO, this will be Move.NONE.
// appliedMove: Move given by the learner for the most recent image

public class RobotMessage implements TaggedMessage {
	private Mode inThisMode;
	private Move trainedMove;
	private Move appliedMove;
	private byte tag;
	
	public static final int NUM_BYTES = 4;
	
	public Mode getCurrentMode() {return inThisMode;}
	public Move getTrainedMove() {return trainedMove;}
	public Move getAppliedMove() {return appliedMove;}
	
	public byte getTag() {return tag;}
	public void setTag(byte tag) {this.tag = tag;}
	
	public RobotMessage(Mode current, Move trained, Move applied) {
		this.inThisMode = current;
		this.trainedMove = trained;
		this.appliedMove = applied;
		this.tag = 0;
	}
	
	public RobotMessage(byte[] bytes) {
		inThisMode = Mode.values()[bytes[0]];
		trainedMove = Move.values()[bytes[1]];
		appliedMove = Move.values()[bytes[2]];
		tag = bytes[3];
	}
	
	public byte[] toBytes() {
		byte[] result = new byte[NUM_BYTES];
		result[0] = (byte)inThisMode.ordinal();
		result[1] = (byte)trainedMove.ordinal();
		result[2] = (byte)appliedMove.ordinal();
		result[3] = tag;
		return result;
	}
	
	@Override
	public String toString() {
		return String.format("RobotMessage: Mode: %s TrainedMove: %s AppliedMove: %s Tag: %d", inThisMode, trainedMove, appliedMove, tag);
	}
}
