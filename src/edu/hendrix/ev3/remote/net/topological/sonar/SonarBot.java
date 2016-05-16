package edu.hendrix.ev3.remote.net.topological.sonar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;

import edu.hendrix.ev3.ai.topological.StateMap;
import edu.hendrix.ev3.ai.topological.sonar.ClusterableSonarState;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.NetBot;
import edu.hendrix.ev3.remote.net.NetBotCommand;
import edu.hendrix.ev3.remote.sonar.ThreeSonarBot;
import edu.hendrix.ev3.util.Logger;
import edu.hendrix.ev3.util.StampedStorage;

public class SonarBot extends NetBot {
	public static void main(String[] args) throws IOException {
		new SonarBot().mainLoop();
	}
	
	private ThreeSonarBot bot;
	private CommandMessage lastCmd = new CommandMessage(Command.STOP);
	private StateMap<ClusterableSonarState> learner;
	private ClusterableSonarState currentSonar;
	private int currentState;
	private LocalDateTime saveStamp;

	public SonarBot() throws IOException {
		super(CommandMessage.NUM_BYTES, RobotMessage.NUM_BYTES);
	}

	@Override
	public void setup() {
		bot = new ThreeSonarBot();
	}

	@Override
	public void teardown() {
		saveCurrent();
	}

	@Override
	public void checkSensors() {
		bot.pollSonars();
		currentSonar = new ClusterableSonarState(bot);
	}

	@Override
	public NetBotCommand selectMoveAndReply(byte[] receivedMessage) {
		boolean quit = false, replying = false;
		if (receivedMessage.length > 0) {
			replying = true;
			lastCmd = CommandMessage.fromBytes(receivedMessage);
			Logger.EV3Log.log("lastCmd: " + lastCmd);
			switch (lastCmd.getCommand()) {
			case QUIT:
				quit = true; 
				break;
			case NEW_BSOC:
				saveCurrent();
				learner = ClusterableSonarState.makeStateMap(lastCmd.getNumNodes());
				Logger.EV3Log.log(String.format("Starting learner: Nodes: %d", lastCmd.getNumNodes()));
				lastCmd.replaceCommandWith(Command.STOP);
				currentState = learner.getStartingLabel();
				break;
			case LOAD_BSOC:
			case ARCHIVES:
				saveCurrent();
				throw new UnsupportedOperationException("Not implemented yet");
			default:
				// Intentionally left blank	
			}
			saveStamp = lastCmd.getTimestamp();
		}
		
		if (isInitialized() && getCurrentMove() != Move.STOP) {
			Logger.EV3Log.format("Training: currentState: %d, currentMove: %s", currentState, getCurrentMove().toString()); 
			Logger.EV3Log.log(currentSonar.toString());
			currentState = learner.addTransition(currentState, getCurrentMove(), currentSonar);
			Logger.EV3Log.format("state: %d", currentState);
		}

		if (replying) {
			RobotMessage reply = new RobotMessage(lastCmd.getTag(), isInitialized(), currentSonar);
			return new NetBotCommand(lastCmd.getCommand().getMove(), reply.toBytes(), quit);
		} else {
			return new NetBotCommand(lastCmd.getCommand().getMove());
		}
	}
	
	boolean isInitialized() {
		return learner != null;
	}
	
	void saveCurrent() {
		if (isInitialized()) {
			try {
				StampedStorage.save(learner, saveStamp);
				//Logger.EV3Log.format("Saving %s; %d chars", saveStamp.toString(), learner.toString().length());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
