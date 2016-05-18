package edu.hendrix.ev3.ai.supervised.evaluable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.supervised.RobotLearner;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.ai.cluster.yuv.YUYVDistanceFuncs;

public class KnearestNeighbors implements RobotLearner {
	private HashMap<AdaptedYUYVImage,Move> neighbors = new HashMap<AdaptedYUYVImage,Move>();
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
		neighbors.put(img, current);
	}
	@Override
	public Move bestMatchFor(AdaptedYUYVImage img) {
		TreeMap<Number, Move> kClosest = new TreeMap<>();
		for (Map.Entry<AdaptedYUYVImage,Move> i: neighbors.entrySet()){
			long dist = distFunction(i.getKey(),img);
			if (kClosest.size() < k){
				kClosest.put(dist, i.getValue());
			}
			else if(kClosest.higherEntry(dist) != null){
				kClosest.put(dist, i.getValue());
				kClosest.pollLastEntry();
			}
		}
		
		return getMostCommonElement(kClosest);
	}
	public long distFunction(AdaptedYUYVImage img1, AdaptedYUYVImage img2){
		// Function subject to change
		long dist = YUYVDistanceFuncs.euclideanAllChannels(img1, img2);
		return dist;
	}
	public Move getMostCommonElement(TreeMap<Number, Move> kClosest){
		HashMap<Move,Integer> occurences = new HashMap<>();
		for (Move m: kClosest.values()){
			if (occurences.containsKey(m)){
				occurences.put(m, occurences.get(m)+1);
			}
			else{
				occurences.put(m, 1);
			}
		}
		int maxValue = 0;
		Move maxMove = Move.NONE;
		for (Entry<Move, Integer> m1: occurences.entrySet()){
			if (maxValue < m1.getValue()){
				maxValue = m1.getValue();
				maxMove = m1.getKey();
			}
		}
		return maxMove;
	}

	@Override
	public boolean isTrained() {
		// TODO Auto-generated method stub
		return neighbors.size() > k;
	}
	

	
}
