package edu.hendrix.ev3.ai.supervised;

import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Util;

import java.util.ArrayList;

import edu.hendrix.ev3.ai.bsoc.BoundedSelfOrgCluster;
import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.cluster.yuv.YUYVDistanceFuncs;

public class BSOCMoves extends SupervisedClusterer<AdaptedYUYVImage,Move> implements ClusteredRobotLearner {
	private int maxNodesPerCluster;
	
	public BSOCMoves(int maxNodesPerCluster) {
		super(Move.class, dist -> new BoundedSelfOrgCluster<>(maxNodesPerCluster, dist), YUYVDistanceFuncs::euclideanAllChannels);
		this.maxNodesPerCluster = maxNodesPerCluster;
	}
	
	public static BSOCMoves fromString(String src) {
		ArrayList<String> parts = Util.debrace(src);
		return new BSOCMoves(parts.get(1), Integer.parseInt(parts.get(0)));
	}
	
	public BSOCMoves(String src, int maxNodesPerCluster) {
		super(src, Move::valueOf, s -> new BoundedSelfOrgCluster<AdaptedYUYVImage>(s, AdaptedYUYVImage::fromString, YUYVDistanceFuncs::euclideanAllChannels), Move.class, dist -> new BoundedSelfOrgCluster<>(maxNodesPerCluster, dist), YUYVDistanceFuncs::euclideanAllChannels);
		this.maxNodesPerCluster = maxNodesPerCluster;
	}
	
	@Override
	public String toString() {
		return "{" + maxNodesPerCluster + "}{" + super.toString() + "}";
	}
}
