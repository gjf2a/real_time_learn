package edu.hendrix.ev3.ai.supervised;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.Move;

public class ShrinkingLearner implements RobotLearner {
	private RobotLearner rl;
	private int shrinkFactor;
	
	public ShrinkingLearner(RobotLearner rl, int shrinkFactor) {
		this.rl = rl;
		this.shrinkFactor = shrinkFactor;
	}
	
	public AdaptedYUYVImage shrink(AdaptedYUYVImage img) {
		return img.shrunken(shrinkFactor);
	}

	@Override
	public void train(AdaptedYUYVImage img, Move current) {
		rl.train(shrink(img), current);
	}

	@Override
	public Move bestMatchFor(AdaptedYUYVImage img) {
		return rl.bestMatchFor(shrink(img));
	}

	@Override
	public boolean isTrained() {
		return rl.isTrained();
	}
	
	public int getShrinkFactor() {return shrinkFactor;}
}
