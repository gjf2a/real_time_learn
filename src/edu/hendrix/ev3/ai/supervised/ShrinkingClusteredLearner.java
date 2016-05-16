package edu.hendrix.ev3.ai.supervised;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import edu.hendrix.ev3.ai.cluster.DistanceFunc;
import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Triple;

public class ShrinkingClusteredLearner extends ShrinkingLearner implements ClusteredRobotLearner {
	private ClusteredRobotLearner crl;
	
	public ShrinkingClusteredLearner(ClusteredRobotLearner crl, int shrinkFactor) {
		super(crl, shrinkFactor);
		this.crl = crl;
	}
	
	@Override
	public Triple<Move, Integer, Long> bestMatchNodeDistanceFor(AdaptedYUYVImage example) {
		return crl.bestMatchNodeDistanceFor(shrink(example));
	}

	@Override
	public int numClustersWith(Move label) {
		return crl.numClustersWith(label);
	}

	@Override
	public AdaptedYUYVImage getIdealInputFor(Move label, int node) {
		return crl.getIdealInputFor(label, node);
	}

	@Override
	public Set<Move> allLabels() {
		return crl.allLabels();
	}

	@Override
	public Collection<Integer> getClusterIdsFor(Move label) {
		return crl.getClusterIdsFor(label);
	}

	@Override
	public int size() {
		return crl.size();
	}

	@Override
	public DistanceFunc<AdaptedYUYVImage> getDistanceFunc() {
		return crl.getDistanceFunc();
	}

	@Override
	public ArrayList<Triple<Move, Integer, Long>> totalNodeRankingFor(AdaptedYUYVImage example) {
		return crl.totalNodeRankingFor(shrink(example));
	}
	
	@Override
	public String toString() {
		return OUTPUT_PREFIX + ":{" + getShrinkFactor() + "}{" + crl.toString() + '}';
	}
	
	public static final String OUTPUT_PREFIX = "shrink";
}
