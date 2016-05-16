package edu.hendrix.ev3.ai.supervised;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.function.Supplier;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.EnumHistogram;

public class Bagger implements RobotLearner {
	private ArrayList<RobotLearner> learners;
	private double bagProb;
	
	public Bagger(Supplier<RobotLearner> learnerMaker) {
		this(learnerMaker, 10, 0.67);
	}
	
	public Bagger(Supplier<RobotLearner> learnerMaker, int numBags, double trainingProb) {
		learners = new ArrayList<>();
		for (int i = 0; i < numBags; i++) {
			learners.add(learnerMaker.get());
		}
		bagProb = trainingProb;
	}

	@Override
	public void train(AdaptedYUYVImage img, Move current) {
		for (RobotLearner learner: learners) {
			if (Math.random() < bagProb) {
				learner.train(img, current);
			}
		}
	}

	@Override
	public Move bestMatchFor(AdaptedYUYVImage img) {
		EnumHistogram<Move> counts = new EnumHistogram<>(Move.class);
		for (RobotLearner learner: learners) {
			counts.bump(learner.bestMatchFor(img));
		}
		Duple<Move,Integer> best = new Duple<>(Move.NONE, 0);
		for (Entry<Move, Integer> ent: counts) {
			if (ent.getValue() > best.getSecond()) {
				best = new Duple<>(ent.getKey(), ent.getValue());
			}
		}
		return best.getFirst();
	}

	@Override
	public boolean isTrained() {
		for (RobotLearner learner: learners) {
			if (!learner.isTrained()) {return false;}
		}
		return true;
	}
}
