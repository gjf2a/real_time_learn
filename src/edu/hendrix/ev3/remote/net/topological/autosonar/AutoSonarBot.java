package edu.hendrix.ev3.remote.net.topological.autosonar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;

import edu.hendrix.ev3.ai.topological.MoveSelector;
import edu.hendrix.ev3.ai.topological.Path;
import edu.hendrix.ev3.ai.topological.sonar.ClusterableSonarState;
import edu.hendrix.ev3.ai.topological.sonar.SonarConstraints;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.NetBot;
import edu.hendrix.ev3.remote.net.NetBotCommand;
import edu.hendrix.ev3.remote.sonar.ThreeSonarTouchBot;
import edu.hendrix.ev3.util.Logger;
import edu.hendrix.ev3.util.StampedStorage;

public class AutoSonarBot extends NetBot {
	public static void main(String[] args) throws IOException {
		new AutoSonarBot().mainLoop();
	}
	
	public AutoSonarBot() throws IOException {
		super(Message.NUM_BYTES, Response.NUM_BYTES);
	}
	
	private ThreeSonarTouchBot bot;
	private MoveSelector<ClusterableSonarState> learner;
	private ClusterableSonarState currentSonar;
	private SonarConstraints goal;
	private LocalDateTime saveStamp;
	private Move atGoal;
	private Move lastCommand = Move.NONE;
	
	@Override
	public void setup() {
		bot = new ThreeSonarTouchBot();
	}

	@Override
	public void teardown() {
		outputTouch();
		saveCurrent();
	}
	
	private void outputTouch() {
		int touches = bot.getNumTouches();
		int cycles = bot.getNumCycles();
		double percent = 100.0 * touches / cycles;
		Logger.EV3Log.format("Touches: %d/%d (%6.2f%%)", touches, cycles, percent);		
	}

	@Override
	public void checkSensors() {
		bot.pollSonars();
		currentSonar = new ClusterableSonarState(bot);
	}

	@Override
	public NetBotCommand selectMoveAndReply(byte[] receivedMessage) {
		if (receivedMessage.length > 0) {
			Message msg = Message.fromBytes(receivedMessage);
			if (msg.isStarting()) {
				startup(msg);
			} else if (!msg.keepGoing()) {
				return NetBotCommand.makeQuit();
			} else if (msg.isOverriding()) {
				lastCommand = msg.getMoveAtGoal();
			} else if (msg.isReleasing()) {
				lastCommand = Move.NONE;
			}
		}
		return new NetBotCommand(pickMove());
	}
	
	void startup(Message msg) {
		learner = new MoveSelector<>(ClusterableSonarState::makeStateMap, msg.getNumNodes(), msg.getExplorationConstant(), msg.getAllowedMoves());
		goal = msg.getGoal();
		saveStamp = msg.getStamp();
		atGoal = msg.getMoveAtGoal();
		Logger.EV3Log.format("# nodes: %d; exp const: %d", msg.getNumNodes(), msg.getExplorationConstant());
		Logger.EV3Log.format("Allowed moves: %s", msg.getAllowedMoves().toString());
		Logger.EV3Log.format("Goal: %s", msg.getGoal().toString());		
		print("Starting!");
	}

	Move pickMove() {
		Move currentMove = Move.STOP;
		if (isInitialized()) {
			if (lastCommand != Move.NONE) {
				currentMove = lastCommand;
				Logger.EV3Log.log("Override");
			} else if (goal.test(currentSonar)) {
				currentMove = atGoal;
				Logger.EV3Log.log("At goal");
			} else if (Math.random() < learner.chanceToExplore()) {
				currentMove = learner.getExploringMove();
				Logger.EV3Log.log("Exploring");
			} else {
				Path plan = learner.pathTo(currentSonar, goal);
				if (plan.exists()) {
					currentMove = plan.getFirstMove();
					Logger.EV3Log.log("Plan: " + plan);
				} else {
					currentMove = learner.getMostRecentMove();
					Logger.EV3Log.log("No plan; retaining " + currentMove);
				}
			}
			learner.train(currentMove, currentSonar);
			Logger.EV3Log.format("Training step: currentMove is %s", currentMove);
			Logger.EV3Log.format("State: %d; Sensors: %s", learner.getCurrentState(), currentSonar.toString());
			Logger.EV3Log.log(bot.wasPressed() ? "Touching" : "Clear");
		} 
		return currentMove;		
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
