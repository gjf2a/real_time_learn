package edu.hendrix.ev3.remote.net.topological.autosonar;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;

import edu.hendrix.ev3.ai.topological.MoveSet;
import edu.hendrix.ev3.ai.topological.sonar.SonarConstraints;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.ReturnableTaggedMessage;
import edu.hendrix.ev3.util.StampedStorage;

public class Message extends ReturnableTaggedMessage {
	private byte tag;
	private Cmd cmd;
	private int numNodes;
	private int exploreConstant;
	private MoveSet allowedMoves;
	private Move moveAtGoal;
	private LocalDateTime stamp;
	private SonarConstraints goal;
	
	private static byte nextTag = 1;
	
	public final static int NUM_BYTES = 3 + 3 * Integer.BYTES + StampedStorage.DATE_TIME_BYTES + SonarConstraints.NUM_BYTES;
	
	private Message(byte tag, Cmd cmd, int numNodes, int exploreConstant, MoveSet moves, Move moveAtGoal, LocalDateTime stamp, SonarConstraints goal) {
		this.tag = tag;
		this.cmd = cmd;
		this.numNodes = numNodes;
		this.exploreConstant = exploreConstant;
		this.allowedMoves = moves;
		this.moveAtGoal = moveAtGoal;
		this.stamp = stamp;
		this.goal = goal;
	}
	
	public Message(Cmd cmd, int numNodes, int exploreConstant, MoveSet moves, Move moveAtGoal, LocalDateTime stamp, SonarConstraints goal) {
		this(nextTag++, cmd, numNodes, exploreConstant, moves, moveAtGoal, stamp, goal);
	}
	
	public static Message makeOverride(Move overrideMove) {
		return new Message(Cmd.OVERRIDE, 0, 0, new MoveSet(), overrideMove, LocalDateTime.now(), new SonarConstraints());
	}
	
	public static Message makeRelease() {
		return new Message(Cmd.AUTO, 0, 0, new MoveSet(), Move.NONE, LocalDateTime.now(), new SonarConstraints());		
	}
	
	public byte[] toBytes() {
		ByteBuffer result = ByteBuffer.allocate(NUM_BYTES);
		result.put(getTag());
		result.put((byte)cmd.ordinal());
		result.putInt(numNodes);
		result.putInt(exploreConstant);
		result.putInt(allowedMoves.toInt());
		result.put((byte)moveAtGoal.ordinal());
		StampedStorage.putInto(stamp, result);
		result.put(goal.toBytes());
		return result.array();
	}
	
	public static Message fromBytes(byte[] bytes) {
		ByteBuffer input = ByteBuffer.wrap(bytes);
		byte tag = input.get();
		Cmd cmd = Cmd.values()[input.get()];
		int numNodes = input.getInt();
		int exploreConstant = input.getInt();
		MoveSet moves = MoveSet.fromInt(input.getInt());
		Move moveAtGoal = Move.values()[input.get()];
		LocalDateTime stamp = StampedStorage.getFrom(input);
		byte[] goalBytes = new byte[input.remaining()];
		input.get(goalBytes);
		SonarConstraints goal = SonarConstraints.fromBytes(goalBytes);
		return new Message(tag, cmd, numNodes, exploreConstant, moves, moveAtGoal, stamp, goal);
	}

	@Override
	public byte getTag() {
		return tag;
	}

	@Override
	public boolean keepGoing() {
		return cmd != Cmd.END;
	}
	
	public boolean isStarting() {
		return cmd == Cmd.START;
	}
	
	public int getNumNodes() {
		return numNodes;
	}
	
	public int getExplorationConstant() {
		return exploreConstant;
	}
	
	public MoveSet getAllowedMoves() {
		return allowedMoves;
	}
	
	public SonarConstraints getGoal() {
		return goal;
	}
	
	public Move getMoveAtGoal() {
		return moveAtGoal;
	}
	
	public LocalDateTime getStamp() {
		return stamp;
	}
	
	public boolean isOverriding() {
		return cmd == Cmd.OVERRIDE;
	}
	
	public boolean isReleasing() {
		return cmd == Cmd.AUTO;
	}
}
