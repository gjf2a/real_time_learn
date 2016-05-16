package edu.hendrix.ev3.remote.net.topological;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

import edu.hendrix.ev3.remote.net.ReturnableTaggedMessage;
import edu.hendrix.ev3.util.StampedStorage;

public class CommandMessage extends ReturnableTaggedMessage {
	private Command cmd;
	private int target, shrinkFactor;
	private byte tag;
	private LocalDateTime timeStamp;
	
	private static byte nextTag = 1;
	
	public static final int NUM_BYTES = 2 + 2 * Integer.BYTES + StampedStorage.DATE_TIME_BYTES;
	
	CommandMessage(Command cmd, byte tag, int target, int shrinkFactor, LocalDateTime timeStamp) {
		this.cmd = cmd;
		this.tag = tag;
		this.target = target;
		this.shrinkFactor = shrinkFactor;
		this.timeStamp = timeStamp;
	}
	
	public void replaceCommandWith(Command replacement) {
		cmd = replacement;
	}
	
	@Override
	public String toString() {
		return String.format("Command:%s;Tag:%d;Target:%d;Shrink:%d;Stamp:%s", cmd.toString(), tag, target, shrinkFactor, timeStamp.toString());
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof CommandMessage) {
			CommandMessage that = (CommandMessage)other;
			return this.cmd == that.cmd && this.tag == that.tag && this.target == that.target && this.shrinkFactor == that.shrinkFactor && this.timeStamp.equals(that.timeStamp);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
	
	public CommandMessage(Command cmd) {
		this(cmd, nextTag++, 0, 1, LocalDateTime.now());
	}
	
	public static CommandMessage makeNavigate(int target) {
		return new CommandMessage(Command.NAVIGATE, nextTag++, target, 1, LocalDateTime.now());
	}
	
	public static CommandMessage makeStartup(int numNodes, int shrinkFactor) {
		return new CommandMessage(Command.NEW_BSOC, nextTag++, numNodes, shrinkFactor, LocalDateTime.now());
	}
	
	public static CommandMessage requestArchives(LocalDateTime timeStamp) {
		return new CommandMessage(Command.ARCHIVES, nextTag++, 0, 0, timeStamp);
	}
	
	public static CommandMessage makeFromArchive(LocalDateTime timeStamp) {
		return new CommandMessage(Command.LOAD_BSOC, nextTag++, 0, 0, timeStamp);
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
	public int getTarget() {return target;}
	public int getShrink() {return shrinkFactor;}
	public LocalDateTime getTimestamp() {return timeStamp;}
	
	@Override
	public boolean keepGoing() {return cmd != Command.QUIT;}
	
	@Override
	public byte getTag() {return tag;}
	
	public static CommandMessage fromBytes(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		Command cmd = Command.values()[buffer.get()];
		byte tag = buffer.get();
		int target = buffer.getInt();
		int shrinkFactor = buffer.getInt();
		byte[] dtbytes = new byte[StampedStorage.DATE_TIME_BYTES];
		buffer.get(dtbytes);
		LocalDateTime stamp = StampedStorage.bytes2LocalDateTime(dtbytes);
		return new CommandMessage(cmd, tag, target, shrinkFactor, stamp);
	}
	
	@Override
	public byte[] toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(NUM_BYTES);
		buffer.put((byte)cmd.ordinal());
		buffer.put(tag);
		buffer.putInt(target);
		buffer.putInt(shrinkFactor);
		StampedStorage.putInto(timeStamp, buffer);
		return buffer.array();
	}
}
