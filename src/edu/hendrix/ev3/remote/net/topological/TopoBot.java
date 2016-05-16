package edu.hendrix.ev3.remote.net.topological;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.TreeSet;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.topological.Path;
import edu.hendrix.ev3.ai.topological.PathsFrom;
import edu.hendrix.ev3.ai.topological.image.ImageStateMap;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.NetBot;
import edu.hendrix.ev3.remote.net.NetBotCommand;
import edu.hendrix.ev3.remote.net.RobotConstants;
import edu.hendrix.ev3.remote.sonar.Wanderer;
import edu.hendrix.ev3.util.CircularList;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.Logger;
import edu.hendrix.ev3.util.StampedStorage;
import lejos.hardware.video.Video;

public class TopoBot extends NetBot {

	private ImageStateMap learner;
	private int currentState, currentGoal;
	private CommandMessage lastCmd = new CommandMessage(Command.STOP);
	private CircularList<AdaptedYUYVImage> photos = new CircularList<>();
	private Wanderer wanderer;
	private LocalDateTime saveStamp;
	private Video v;
	private byte[] frame;
	
	public TopoBot() throws IOException {
		super(CommandMessage.NUM_BYTES, RobotMessage.NUM_BYTES);
	}

	@Override
	public void setup() {
		try {
			wanderer = new Wanderer(0.3f);
			v = RobotConstants.setupEV3Video();
			frame = v.createFrame();
		} catch (Exception exc) {
			throw new IllegalStateException(exc.getMessage());
		}
	}

	@Override
	public void teardown() {
		saveCurrent();
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
		byte[] reply = null;
		if (receivedMessage.length > 0) {
			lastCmd = CommandMessage.fromBytes(receivedMessage);
			switch (lastCmd.getCommand()) {
			case QUIT:
				quit = true; break;
			case NEW_BSOC:
				saveCurrent();
				learner = new ImageStateMap(lastCmd.getTarget(), lastCmd.getShrink());
				currentState = learner.getStartingLabel();
				currentGoal = currentState - 1;
				Logger.EV3Log.log(String.format("Starting learner: Shrink: %d Nodes: %d", lastCmd.getShrink(), lastCmd.getTarget()));
				lastCmd.replaceCommandWith(Command.STOP);
				break;
			case ARCHIVES:
				reply = makeArchiveMessage(lastCmd).toBytes();
				Logger.EV3Log.format("Sending archive titles (earlier than %s)", lastCmd.getTimestamp()); 
				lastCmd.replaceCommandWith(Command.STOP);
				break;
			case LOAD_BSOC:
				try {
					saveCurrent();
					Logger.EV3Log.format("About to load %s", lastCmd.getTimestamp());
					learner = StampedStorage.open(ImageStateMap.class, lastCmd.getTimestamp(), 0, ImageStateMap::fromString);
					currentState = learner.getStateFor(new AdaptedYUYVImage(frame, RobotConstants.WIDTH, RobotConstants.HEIGHT));
					currentGoal = currentState - 1;
					reply = new RobotMessage(lastCmd.getTag(), true).toBytes();
				} catch (FileNotFoundException e) {
					reply = new RobotMessage(lastCmd.getTag(), false).toBytes();
					e.printStackTrace();
				} finally {
					lastCmd.replaceCommandWith(Command.STOP);
				}
				break;
			default:
				// Intentionally left blank
			}
			saveStamp = lastCmd.getTimestamp();
		}
		
		AdaptedYUYVImage img = new AdaptedYUYVImage(frame, RobotConstants.WIDTH, RobotConstants.HEIGHT);
		
		if (isInitialized() && getCurrentMove() != Move.STOP) {
			currentState = learner.addTransition(currentState, getCurrentMove(), img);
			Logger.EV3Log.format("%d clusters", learner.size());
			Logger.EV3Log.format("state: %d", currentState);
			if (photos.size() > 0) {
				currentGoal = learner.getStateFor(photos.getCurrentItem());
				Logger.EV3Log.format("goal: %d at? %s", currentGoal, atGoal() ? "yes" : "no");
			}
		}
		
		Duple<Move,RobotMessage> proc = processLastCommand(img);
		if (reply == null) {reply = proc.getSecond().toBytes();}
		if (quit || reply.length > 0) {
			return new NetBotCommand(proc.getFirst(), reply, quit);
		} else {
			return new NetBotCommand(proc.getFirst());
		}
	}
	
	public boolean atGoal() {return currentGoal == currentState;}
	public boolean isInitialized() {return learner != null;}

	void saveCurrent() {
		if (isInitialized()) {
			try {
				StampedStorage.save(learner, saveStamp);
				Logger.EV3Log.format("Saving %s; %d chars", saveStamp.toString(), learner.toString().length());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	RobotMessage makeArchiveMessage(CommandMessage msg) {
		LocalDateTime cutoff = msg.getTimestamp();
		TreeSet<LocalDateTime> candidates = new TreeSet<>();
		for (Duple<LocalDateTime, Integer> candidate: StampedStorage.getAvailableFor(ImageStateMap.class)) {
			if (candidate.getFirst().compareTo(cutoff) <= 0) {
				candidates.add(candidate.getFirst());
			}
		}
		while (candidates.size() > RobotMessage.MAX_ARCHIVES) {
			candidates.remove(candidates.first());
		}
		return new RobotMessage(msg.getTag(), isInitialized(), candidates);
	}
	
	Duple<Move,RobotMessage> processLastCommand(AdaptedYUYVImage img) {
		Move cmdMove = lastCmd.getCommand().getMove();
		RobotMessage msg = new RobotMessage(lastCmd.getTag(), isInitialized(), atGoal());
		switch (lastCmd.getCommand()) {
		case WANDER:
			cmdMove = wanderer.selectMove();
			break;
		case NAVIGATE:
			cmdMove = navigateMove(img);
			break;
		case PHOTO:
			msg = takePicture(img);
			break;
		default:
			// Intentionally left blank
		}
		Logger.EV3Log.format("lastCmd: %s", lastCmd.getCommand());
		Logger.EV3Log.format("currentMove: %s", cmdMove);
		return new Duple<>(cmdMove, msg);
	}
	
	Move navigateMove(AdaptedYUYVImage img) {
		Move result = Move.STOP;
		if (isInitialized()) {
			PathsFrom paths = learner.bestPathsFrom(img);
			if (!atGoal()) {
				Path plan = paths.pathTo(currentGoal);
				result = plan.getFirstMove();
				Logger.EV3Log.format("Not at goal; selecting %s", result); 
			} 
		}
		return result;
	}
	
	RobotMessage takePicture(AdaptedYUYVImage img) {
		photos.add(new AdaptedYUYVImage(img));
		RobotMessage msg = new RobotMessage(lastCmd.getTag(), isInitialized(), atGoal(), photos.getCurrentIndex(), img);
		lastCmd.replaceCommandWith(Command.STOP);
		return msg;
	}
}
