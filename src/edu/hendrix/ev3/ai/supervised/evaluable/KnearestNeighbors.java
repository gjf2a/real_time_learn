package edu.hendrix.ev3.ai.supervised.evaluable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.supervised.RobotLearner;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.ai.cluster.yuv.YUYVDistanceFuncs;

public class KnearestNeighbors implements RobotLearner {
	private HashMap<Move,ArrayList<AdaptedYUYVImage>> neighbors = new HashMap<>();
	private int k = 1;

	
	public KnearestNeighbors() {
	}
	public KnearestNeighbors(int n){
		k = n;
	}
	public void setK(int n){
		k = n;
	}
	@Override
	public void train(AdaptedYUYVImage img, Move current) {
		if(!neighbors.containsKey(current)){
			neighbors.put(current, new ArrayList<AdaptedYUYVImage>());
		}
		neighbors.get(current).add(img);
	}
	
	@Override
	public Move bestMatchFor(AdaptedYUYVImage img) {
		long minDist = Long.MAX_VALUE;
		AdaptedYUYVImage minImage = null;
		Move minMove = Move.STOP;
		for (Move m: neighbors.keySet()){
			for (AdaptedYUYVImage i: neighbors.get(m)){
				long tempDist = distFunction(i,img);
				if (tempDist < minDist){
					minDist = tempDist;
					minImage = i;
					minMove = m;
				}
			}
		}
		return minMove;
		
	}
	public long distFunction(AdaptedYUYVImage img1, AdaptedYUYVImage img2){
		// Function subject to change
		long dist = YUYVDistanceFuncs.euclideanAllChannels(img1, img2);
		return dist;
	}
	@Override
	public boolean isTrained() {
		// TODO Auto-generated method stub
		return neighbors.size() > k;
	}
	

	
}
