package edu.hendrix.ev3.remote.net.imitation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import edu.hendrix.ev3.remote.net.ReturnableTaggedMessage;

abstract public class UserMessage extends ReturnableTaggedMessage {
	private Mode mode;
	private byte tag;
	
	private static byte nextTag = 1;
	
	public static final int NUM_BYTES = Math.max(StartupMessage.NUM_BYTES, CommandMessage.NUM_BYTES);
	
	abstract public byte[] toBytes();
	
	public UserMessage(Mode mode) {
		this(mode, nextTag++);
	}
	
	protected UserMessage(Mode mode, byte tag) {
		this.mode = mode;
		this.tag = tag;
	}

	public static UserMessage receive(DatagramSocket sock) {
		try {
			byte[] inputs = new byte[NUM_BYTES];
			DatagramPacket input = new DatagramPacket(inputs, inputs.length);
			sock.receive(input);
			UserMessage msg = fromBytes(inputs);
			msg.setSender(input.getAddress());
			return msg;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.getMessage());
		}
	}
	
	public static UserMessage fromBytes(byte[] inputs) {
		Mode incoming = Mode.values()[inputs[0]];
		UserMessage msg = incoming.makeMessageFrom(inputs);
		return msg;
	}
	
	public Mode getMode() {return mode;}
	
	@Override
	public byte getTag() {return tag;}
	
	@Override
	public boolean keepGoing() {
		return getMode().keepGoing();
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
	
	@Override
	public boolean equals(Object other) {return toString().equals(other.toString());}
}
