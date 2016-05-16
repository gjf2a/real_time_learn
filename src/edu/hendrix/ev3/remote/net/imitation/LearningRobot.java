package edu.hendrix.ev3.remote.net.imitation;

import java.io.IOException;
import java.util.ArrayList;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.supervised.BSOCMoves;
import edu.hendrix.ev3.ai.supervised.ClusteredRobotLearner;
import edu.hendrix.ev3.ai.supervised.ShrinkingClusteredLearner;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.NetBot;
import edu.hendrix.ev3.remote.net.NetBotCommand;
import edu.hendrix.ev3.remote.net.RobotConstants;
import edu.hendrix.ev3.storage.Storage;
import edu.hendrix.ev3.storage.YesNoChooser;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.Logger;
import edu.hendrix.ev3.util.Util;
import lejos.hardware.video.Video;

public class LearningRobot extends NetBot {
	public static void main(String[] args) throws IOException {
		new LearningRobot().mainLoop();
	}
	
	private ClusteredRobotLearner learner;
	private Mode mode;
	private Move move;
	private UserMessage lastMsg;
	private LearningPerformanceStats stats;
	private Video v;
	private byte[] frame;
	private Storage<ClusteredRobotLearner> storage;
	
	public LearningRobot() throws IOException {
		super(UserMessage.NUM_BYTES, RobotMessage.NUM_BYTES);
		lastMsg = new CommandMessage(Mode.SHOW, Move.NONE);
		mode = lastMsg.getMode();
		move = Move.NONE;
		stats = new LearningPerformanceStats();
		storage = Storage.getEV3Storage("bsocMoves", "bsocMoves", LearningRobot::extractor);
	}

	@Override
	public void setup() {
		try {
			if (YesNoChooser.answersYes("Use stored behavior?", true)) {
				learner = storage.openMostRecent();
			} 
			v = RobotConstants.setupEV3Video();
			frame = v.createFrame();
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	public void teardown() {
		try {
			print("Saving...");
			storage.save(learner);
			print("Saved");
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	public void checkSensors() {
		try {
			v.grabFrame(frame);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	public NetBotCommand selectMoveAndReply(byte[] receivedMessage) {
		boolean quit = false;
		if (receivedMessage.length > 0) {
			lastMsg = UserMessage.fromBytes(receivedMessage);
			if (lastMsg instanceof StartupMessage) {
				startup((StartupMessage)lastMsg);
			} else {
				unpackCommand((CommandMessage)lastMsg);
				quit = !mode.keepGoing();
			}
		}
		
		RobotMessage reply = controlRobot();
		return new NetBotCommand(move, reply.toBytes(), quit);
	}
	
	void startup(StartupMessage msg) {
		learner = new BSOCMoves(msg.getMaxNodes());
		if (msg.getShrinkFactor() > 1) {
			learner = new ShrinkingClusteredLearner(learner, msg.getShrinkFactor());
		}
		Logger.EV3Log.log(String.format("Starting learner: Shrink: %d Nodes: %d", msg.getShrinkFactor(), msg.getMaxNodes()));
		mode = Mode.SHOW;
		move = Move.NONE;
	}
	
	void setMoveMode(Duple<Move,Mode> fromCmd) {
		move = fromCmd.getFirst();
		mode = fromCmd.getSecond();
	}
	
	void unpackCommand(CommandMessage cmd) {
		setMoveMode(cmd.unpack());
		Logger.EV3Log.log(cmd.toString());
	}
	
	RobotMessage controlRobot() {
		AdaptedYUYVImage img = new AdaptedYUYVImage(frame, RobotConstants.WIDTH, RobotConstants.HEIGHT);
		RobotMessage msg = mode.act(move, img, learner);
		msg.setTag(lastMsg.getTag());
		logControlInfo(msg);
		return msg;
	}
	
	void logControlInfo(RobotMessage msg) {
		stats.record(msg);
		Logger.EV3Log.log(msg.toString());
		if (msg.getCurrentMode() == Mode.TRAIN) {
			for (Move m: stats) {
				Logger.EV3Log.log(stats.getStatsFor(m));
				Logger.EV3Log.format("Move %s: %d clusters", m, learner.numClustersWith(m));
			}
		}
		logFPS();
	}

	public static ClusteredRobotLearner extractor(String s) {
		if (s.startsWith(ShrinkingClusteredLearner.OUTPUT_PREFIX)) {
			ArrayList<String> parts = Util.debrace(s);
			int shrink = Integer.parseInt(parts.get(1));
			return new ShrinkingClusteredLearner(BSOCMoves.fromString(parts.get(2)), shrink);
		} else {
			return BSOCMoves.fromString(s);
		}
	}
}