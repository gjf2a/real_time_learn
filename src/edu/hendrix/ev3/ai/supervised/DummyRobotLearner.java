package edu.hendrix.ev3.ai.supervised;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.Move;

public class DummyRobotLearner implements RobotLearner {

	@Override
	public void train(AdaptedYUYVImage img, Move current) {
	}

	@Override
	public Move bestMatchFor(AdaptedYUYVImage img) {
		return Move.STOP;
	}

	@Override
	public boolean isTrained() {
		return true;
	}
}
