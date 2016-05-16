package edu.hendrix.ev3.ai.supervised;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

import edu.hendrix.ev3.ai.cluster.DistanceFunc;
import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.cluster.yuv.YUYVDistanceFuncs;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.RobotConstants;
import edu.hendrix.ev3.util.Triple;

public class DummyClusteredRobotLearner implements ClusteredRobotLearner {

	@Override
	public void train(AdaptedYUYVImage img, Move current) {}

	@Override
	public Triple<Move, Integer, Long> bestMatchNodeDistanceFor(AdaptedYUYVImage example) {
		return new Triple<>(Move.NONE,0,Long.MAX_VALUE);
	}

	@Override
	public int numClustersWith(Move label) {
		return 0;
	}

	@Override
	public AdaptedYUYVImage getIdealInputFor(Move label, int node) {
		return RobotConstants.makeBlank();
	}

	@Override
	public Move bestMatchFor(AdaptedYUYVImage img) {
		return Move.NONE;
	}

	@Override
	public boolean isTrained() {
		return true;
	}

	@Override
	public Set<Move> allLabels() {
		return EnumSet.noneOf(Move.class);
	}

	@Override
	public ArrayList<Integer> getClusterIdsFor(Move label) {
		return new ArrayList<>();
	}

	@Override
	public int size() {
		return 0;
	}
	
	public DistanceFunc<AdaptedYUYVImage> getDistanceFunc() {return YUYVDistanceFuncs::euclideanAllChannels;}

	@Override
	public ArrayList<Triple<Move, Integer, Long>> totalNodeRankingFor(AdaptedYUYVImage example) {
		return new ArrayList<>();
	}
}
