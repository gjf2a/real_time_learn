package edu.hendrix.ev3.remote.net.actionselector;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;

import edu.hendrix.ev3.remote.net.TaggedMessage;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.FixedSizeArray;
import edu.hendrix.ev3.util.StampedStorage;

public class ActionSelectorReply implements TaggedMessage {
	
	public final static int MAX_STORED = 30;
	public final static int SIZE = 1 + MAX_STORED * (StampedStorage.DATE_TIME_BYTES + Integer.BYTES);
	
	private FixedSizeArray<Duple<LocalDateTime,Integer>> names;
	private byte tag;
	
	public ActionSelectorReply(byte tag) {
		this.tag = tag;
		names = FixedSizeArray.makeImmutableType(MAX_STORED);
	}
	
	public void addName(LocalDateTime name, int suffix) {
		names.add(new Duple<>(name, suffix));
	}
	
	public ArrayList<Duple<LocalDateTime,Integer>> getNames() {
		return names.values();
	}

	@Override
	public byte getTag() {
		return tag;
	}

	@Override
	public byte[] toBytes() {
		ByteBuffer bytes = ByteBuffer.allocate(SIZE);
		bytes.put(tag);
		for (Duple<LocalDateTime,Integer> name: names.values()) {
			StampedStorage.putInto(name.getFirst(), bytes);
			bytes.putInt(name.getSecond());
		}
		return bytes.array();
	}

	public static ActionSelectorReply fromBytes(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		ActionSelectorReply result = new ActionSelectorReply(buffer.get());
		while (buffer.hasRemaining()) {
			result.addName(StampedStorage.getFrom(buffer), buffer.getInt());
		}
		return result;
	}
}
