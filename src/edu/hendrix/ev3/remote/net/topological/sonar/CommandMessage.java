package edu.hendrix.ev3.remote.net.topological.sonar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

import edu.hendrix.ev3.ai.topological.sonar.SonarConstraints;
import edu.hendrix.ev3.remote.net.ReturnableTaggedMessage;
import edu.hendrix.ev3.util.StampedStorage;

public class CommandMessage extends ReturnableTaggedMessage {
	private Command cmd;
	private byte tag;
	private int numNodes;
	private SonarConstraints goal;
	private LocalDateTime timeStamp;
	
	private static byte nextTag = 1;
	
	public static final int NUM_BYTES = 2 + Integer.BYTES + SonarConstraints.NUM_BYTES + StampedStorage.DATE_TIME_BYTES;
	
	CommandMessage(Command cmd, byte tag, int numNodes, SonarConstraints goal, LocalDateTime timeStamp) {
		this.cmd = cmd;
		this.tag = tag;
		this.numNodes = numNodes;
		this.goal = goal;
		this.timeStamp = timeStamp;
	}
	
	public void replaceCommandWith(Command replacement) {
		cmd = replacement;
	}
	
	@Override
	public String toString() {
		return String.format("Command:%s;Tag:%d;Nodes:%d;Goal:%s;Stamp:%s", cmd.toString(), tag, numNodes, goal.toString(), timeStamp.toString());
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof CommandMessage) {
			CommandMessage that = (CommandMessage)other;
			return this.cmd == that.cmd && this.tag == that.tag && this.numNodes == that.numNodes && this.goal.equals(that.goal) && this.timeStamp.equals(that.timeStamp);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
	
	public CommandMessage(Command cmd) {
		this(cmd, nextTag++, 0, new SonarConstraints(), LocalDateTime.now());
	}
	
	public static CommandMessage makeNavigate(SonarConstraints target) {
		return new CommandMessage(Command.NAVIGATE, nextTag++, 0, target, LocalDateTime.now());
	}
	
	public static CommandMessage makeStartup(int numNodes) {
		return new CommandMessage(Command.NEW_BSOC, nextTag++, numNodes, new SonarConstraints(), LocalDateTime.now());
	}
	
	public static CommandMessage requestArchives(LocalDateTime timeStamp) {
		return new CommandMessage(Command.ARCHIVES, nextTag++, 0, new SonarConstraints(), timeStamp);
	}
	
	public static CommandMessage makeFromArchive(LocalDateTime timeStamp) {
		return new CommandMessage(Command.LOAD_BSOC, nextTag++, 0, new SonarConstraints(), timeStamp);
	}
	
	public static CommandMessage receive(DatagramSocket sock) {
		try {
			byte[] inputs = new byte[NUM_BYTES];
			DatagramPacket input = new DatagramPacket(inputs, inputs.length);
			sock.receive(input);
			CommandMessage msg = CommandMessage.fromBytes(inputs);
			msg.setSender(input.getAddress());
			return msg;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.getMessage());
		}
	}
	
	public Command getCommand() {return cmd;}
	public int getNumNodes() {return numNodes;}
	public SonarConstraints getGoal() {return goal;}
	public LocalDateTime getTimestamp() {return timeStamp;}
	
	@Override
	public boolean keepGoing() {return cmd != Command.QUIT;}
	
	@Override
	public byte getTag() {return tag;}
	
	public static CommandMessage fromBytes(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		Command cmd = Command.values()[buffer.get()];
		byte tag = buffer.get();
		int numNodes = buffer.getInt();
		byte[] conBytes = new byte[SonarConstraints.NUM_BYTES];
		buffer.get(conBytes);
		byte[] stampBytes = new byte[StampedStorage.DATE_TIME_BYTES];
		buffer.get(stampBytes);
		return new CommandMessage(cmd, tag, numNodes, SonarConstraints.fromBytes(conBytes), StampedStorage.bytes2LocalDateTime(stampBytes));
	}
	
	@Override
	public byte[] toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(NUM_BYTES);
		buffer.put((byte)cmd.ordinal());
		buffer.put(tag);
		buffer.putInt(numNodes);
		buffer.put(goal.toBytes());
		StampedStorage.putInto(timeStamp, buffer);
		return buffer.array();
	}
}
