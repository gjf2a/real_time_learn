package edu.hendrix.ev3.remote.net.simplest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import edu.hendrix.ev3.remote.net.RobotConstants;
import edu.hendrix.ev3.remote.net.ToRobot;
import edu.hendrix.ev3.remote.net.UpdateListener;
import edu.hendrix.ev3.util.Logger;

public class SimpleMoveClient {
	private DatagramSocket msgSock;
	private ArrayList<UpdateListener<String>> statusListeners = new ArrayList<>();
	
	public SimpleMoveClient() throws SocketException, UnknownHostException {
		this.msgSock = new DatagramSocket(RobotConstants.MSG_PORT);
	}
	
	public void addStatusListener(UpdateListener<String> listener) {
		statusListeners.add(listener);
	}

	public void oneWaySend(ToRobot cmd) {
		try {
			byte[] data = new byte[]{cmd.code()};
			DatagramPacket info = new DatagramPacket(data, data.length, InetAddress.getByAddress(RobotConstants.addr), RobotConstants.MSG_PORT);
			Logger.ClientLog.log("Sending " + cmd);
			msgSock.send(info);
			Logger.ClientLog.log("(sent " + cmd + ")");
		} catch (IOException ioe) {
			notifyStatus(ioe);
		}
	}	
	
	private void notifyStatus(IOException ioe) {
		Logger.ClientLog.log("Exception: " + ioe.getMessage());
		ioe.printStackTrace();
		for (UpdateListener<String> listener: statusListeners) {
			listener.report(ioe.getMessage());
		}
	}
	
	public void quit() throws IOException {
		oneWaySend(ToRobot.QUIT);
		msgSock.close();
	}
	
	public static void main(String[] args) throws InterruptedException, IOException {
		SimpleMoveClient client = new SimpleMoveClient();
		for (String cmd: args) {
			client.oneWaySend(ToRobot.valueOf(cmd));
			Thread.sleep(1000);
		}
		client.quit();
	}
}
