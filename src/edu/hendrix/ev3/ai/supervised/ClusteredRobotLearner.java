package edu.hendrix.ev3.ai.supervised;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import edu.hendrix.ev3.ai.cluster.DistanceFunc;
import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.Triple;

public interface ClusteredRobotLearner extends RobotLearner {
	public Triple<Move, Integer, Long> bestMatchNodeDistanceFor(AdaptedYUYVImage example);
	public int numClustersWith(Move label);
	public AdaptedYUYVImage getIdealInputFor(Move label, int node);
	public Set<Move> allLabels();
	public Collection<Integer> getClusterIdsFor(Move label);
	public int size();
	public DistanceFunc<AdaptedYUYVImage> getDistanceFunc();
	public ArrayList<Triple<Move,Integer,Long>> totalNodeRankingFor(AdaptedYUYVImage example);

	default public Move bestMatchFor(AdaptedYUYVImage img) {
		return bestMatchNodeDistanceFor(img).getFirst();
	}
	
	default public AdaptedYUYVImage getIdealInputFor(Duple<Move,Integer> labelAndNode) {
		return getIdealInputFor(labelAndNode.getFirst(), labelAndNode.getSecond());
	}
	
	default public long closestDistanceTo(AdaptedYUYVImage img, Move label) {
		long closest = Long.MAX_VALUE;
		for (int cluster: getClusterIdsFor(label)) {
			long dist = getDistanceFunc().distance(img, getIdealInputFor(label, cluster));
			if (dist < closest) {closest = dist;}
		}
		return closest;
	}
}
