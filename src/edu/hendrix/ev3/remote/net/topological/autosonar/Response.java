package edu.hendrix.ev3.remote.net.topological.autosonar;

import edu.hendrix.ev3.remote.net.TaggedMessage;

public class Response implements TaggedMessage {
	private byte tag;
	
	public final static int NUM_BYTES = 1;
	
	public Response(byte tag) {this.tag = tag;}
	
	public Response(byte[] bytes) {this.tag = bytes[0];}

	@Override
	public byte getTag() {
		return tag;
	}

	@Override
	public byte[] toBytes() {
		return new byte[]{tag};
	}

}
