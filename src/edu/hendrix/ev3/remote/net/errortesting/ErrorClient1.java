package edu.hendrix.ev3.remote.net.errortesting;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ErrorClient1 {
	
	public static final byte[] addr = new byte[]{10,0,1,1};
	
	public static boolean passesCheck(int last, int current) {
		return (current - last == 1) || (last == 9 && current == 0);
	}
	
	public static void main(String[] args) throws IOException {
		DatagramSocket sock = new DatagramSocket(ErrorServer1.PORT);
		byte[] out = new byte[1];
		DatagramPacket pack = new DatagramPacket(out, out.length, InetAddress.getByAddress(addr), ErrorServer1.PORT);
		for (int i = 0; i < 1000; i++) {
			out[0] = (byte)(i % 10);
			sock.send(pack);
		}
		out[0] = ErrorServer1.QUIT;
		sock.send(pack);
		sock.close();
	}
}
