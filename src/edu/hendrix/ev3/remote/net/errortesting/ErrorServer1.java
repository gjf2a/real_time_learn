package edu.hendrix.ev3.remote.net.errortesting;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

public class ErrorServer1 {
	public static final int PORT = 8888;
	public static final byte QUIT = -128;
	
	public static void main(String[] args) throws IOException {
		byte[] value = new byte[1];
		byte last = 9;
		DatagramSocket sock = new DatagramSocket(PORT);
		DatagramPacket incoming = new DatagramPacket(value, value.length);
		int i = 0;
		int errors = 0;
		LCD.clear();
		LCD.drawString("Ready", 0, 0);
		do {
			sock.receive(incoming);
			i += 1;
			if (!ErrorClient1.passesCheck(last, value[0])) {
				errors += 1;
			}
			last = value[0];
			LCD.drawString(String.format("%d/%d     ", errors, i), 0, 0);
		} while (value[0] != QUIT);
		sock.close();
		LCD.drawString("done", 0, 1);
		while (!Button.ESCAPE.isDown());
	}
}
