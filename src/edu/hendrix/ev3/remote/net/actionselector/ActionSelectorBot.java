package edu.hendrix.ev3.remote.net.actionselector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.NetBot;
import edu.hendrix.ev3.remote.net.NetBotCommand;
import edu.hendrix.ev3.remote.net.RobotConstants;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.Logger;
import edu.hendrix.ev3.util.StampedStorage;
import lejos.hardware.video.Video;

public class ActionSelectorBot extends NetBot {
	
	public static void main(String[] args) throws IOException {
		new ActionSelectorBot().mainLoop();
	}
	
	private BSOCController ai;
	private LocalDateTime aiTimestamp;
	private byte lastTag;
	private Move lastMove;
	private Mode mode;
	private Video video;
	private byte[] frame;
	
	public ActionSelectorBot() throws IOException {
		super(ActionSelectorCommand.SIZE, ActionSelectorReply.SIZE);
	}

	@Override
	public void setup() {
		Logger.EV3Log.log("Starting up...");
		try {
			lastMove = Move.STOP;
			mode = Mode.WAITING;
			video = RobotConstants.setupEV3Video();
			frame = video.createFrame();
		} catch (IOException ioe) {
			Logger.EV3Log.format("Could not set up video: %s", ioe.getMessage());
			throw new IllegalStateException(ioe.getMessage());
		}
	}

	@Override
	public void teardown() {
		try {
			StampedStorage.save(ai, aiTimestamp);
		} catch (FileNotFoundException e) {
			Logger.EV3Log.log("Exception when saving: " + e.getMessage());
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	public void checkSensors() {
		try {
			video.grabFrame(frame);
		} catch (IOException ioe) {
			Logger.EV3Log.format("Exception when grabbing: %s", ioe.getMessage());
			throw new IllegalStateException(ioe.getMessage());
		}
	}
	
	public AdaptedYUYVImage wrapBytes() {
		return new AdaptedYUYVImage(frame, RobotConstants.WIDTH, RobotConstants.HEIGHT);
	}

	@Override
	public NetBotCommand selectMoveAndReply(byte[] receivedMessage) {
		if (receivedMessage.length > 0) {
			decodeCmd(new ActionSelectorCommand(receivedMessage));
		}
		
		if (mode == Mode.LEARNING) {
			Logger.EV3Log.format("learning; %s", lastMove.toString()); 
			if (ai != null) {
				ai.train(wrapBytes());
			}
			return new NetBotCommand(lastMove);
			
		} else if (mode == Mode.RETRIEVING) {
			Logger.EV3Log.format("retrieving"); 
			ActionSelectorReply reply = new ActionSelectorReply(lastTag);
			for (Duple<LocalDateTime, Integer> name: StampedStorage.getAvailableFor(BSOCController.class)) {
				reply.addName(name.getFirst(), name.getSecond());
			}
			return new NetBotCommand(Move.STOP, reply.toBytes(), false);
			
		} else if (mode == Mode.APPLYING) {
			lastMove = ai.pickMoveFor(wrapBytes());
			Logger.EV3Log.log("applying " + lastMove);
			return new NetBotCommand(lastMove);
			
		} else if (mode == Mode.QUIT) {
			Logger.EV3Log.log("quitting");
			return NetBotCommand.makeQuit();
			
		} else {
			Logger.EV3Log.format("In mode %s and doing nothing", mode.toString());
			return new NetBotCommand(Move.STOP);
		}
	}
	
	private void decodeCmd(ActionSelectorCommand cmd) {
		lastMove = cmd.getMove();
		lastTag = cmd.getTag();
		Mode updated = cmd.getMode();
		Logger.EV3Log.format("Message received: %s", updated.toString());
		if (updated == Mode.STARTING) {
			ai = new BSOCController(cmd.getNumClusters(), cmd.getShrinkFactor());
			aiTimestamp = cmd.getStamp();
			mode = Mode.LEARNING;
			print("c:" + cmd.getNumClusters() + ";s:" + cmd.getShrinkFactor());
			resetTimer();
			Logger.EV3Log.format("Starting: clusters: %d, shrink: %d", cmd.getNumClusters(), cmd.getShrinkFactor());
		} else {
			mode = updated;
			if (updated == Mode.APPLYING) {
				try {
					ai = StampedStorage.open(BSOCController.class, cmd.getStamp(), cmd.getSuffix(), BSOCController::fromString);
				} catch (FileNotFoundException e) {
					Logger.EV3Log.log("Can't open " + cmd.getStamp());
				}
			}
		}
	}
}