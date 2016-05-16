package edu.hendrix.ev3.remote.net.imitation;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.supervised.ClusteredRobotLearner;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.Mover;
import edu.hendrix.ev3.util.Logger;
import edu.hendrix.ev3.util.Triple;
import lejos.hardware.lcd.LCD;

public enum Mode {
	START {
		@Override
		public UserMessage makeMessageFrom(byte[] bytes) {
			return StartupMessage.fromBytes(bytes);
		}
		
		@Override
		public RobotMessage act(Move userMove, AdaptedYUYVImage img, ClusteredRobotLearner learner) {
			return new RobotMessage(START, userMove, Move.NONE);
		}
		
	}, SHOW {
		@Override
		public RobotMessage act(Move userMove, AdaptedYUYVImage img, ClusteredRobotLearner learner) {
			Mover.move(userMove);
			if (learnerReady(learner)) {
				Move autoMove = learner.bestMatchFor(img);
				show(autoMove);
				return new RobotMessage(SHOW, userMove, autoMove);
			} else {
				return new RobotMessage(SHOW, userMove, Move.NONE);
			}
		}
	}, AUTO {
		@Override
		public RobotMessage act(Move userMove, AdaptedYUYVImage img, ClusteredRobotLearner learner) {
			if (learnerReady(learner)) {
				Triple<Move, Integer, Long> learned = learner.bestMatchNodeDistanceFor(img);
				Logger.EV3Log.format("Cluster: %d", learned.getSecond());
				Move autoMove = learned.getFirst();
				Mover.move(autoMove);
				show(autoMove);
				return new RobotMessage(AUTO, Move.NONE, autoMove);
			} else {
				return new RobotMessage(AUTO, Move.NONE, Move.NONE);
			}
		}
	}, QUIT {
		@Override
		public boolean keepGoing() {return false;}

		@Override
		public RobotMessage act(Move userMove, AdaptedYUYVImage img, ClusteredRobotLearner learner) {
			Mover.move(Move.STOP);
			return new RobotMessage(QUIT, Move.STOP, Move.NONE);
		}
	}, TRAIN {
		@Override
		public RobotMessage act(Move userMove, AdaptedYUYVImage img, ClusteredRobotLearner learner) {
			Mover.move(userMove);
			return trainHelp(userMove, img, learner);
		}
	}, BAD_TRAIN {
		@Override
		public RobotMessage act(Move userMove, AdaptedYUYVImage img, ClusteredRobotLearner learner) {
			Mover.move(userMove);
			RobotMessage msg = trainHelp(Math.random() < 0.5 ? Move.LEFT : Move.FORWARD, img, learner);
			return new RobotMessage(TRAIN, userMove, msg.getAppliedMove());
		}
	};
	
	private static RobotMessage trainHelp(Move userMove, AdaptedYUYVImage img, ClusteredRobotLearner learner) {
		Move learnedMove = Move.NONE;
		if (learner != null && isLearningMove(userMove)) {
			if (learner.isTrained()) {
				learnedMove = learner.bestMatchFor(img);
			}
			learner.train(img, userMove);
		} 
		return new RobotMessage(TRAIN, userMove, learnedMove);		
	}
	
	public UserMessage makeMessageFrom(byte[] bytes) {
		return new CommandMessage(bytes);
	}
	
	public boolean keepGoing() {return true;}
	
	public void show(Move move) {
		LCD.drawString(move.toString() + "     ", 0, 7);
		Logger.EV3Log.format("In %s learned_move: %s", toString(), move);
	}
	
	public static boolean learnerReady(ClusteredRobotLearner learner) {
		return learner != null && learner.isTrained();
	}
	
	public static boolean isLearningMove(Move move) {
		return move != Move.NONE && move != Move.STOP;
	}
	
	abstract public RobotMessage act(Move userMove, AdaptedYUYVImage img, ClusteredRobotLearner learner);
}
