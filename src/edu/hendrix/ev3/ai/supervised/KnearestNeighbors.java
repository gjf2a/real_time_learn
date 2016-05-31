package edu.hendrix.ev3.ai.supervised;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.ai.cluster.yuv.YUYVDistanceFuncs;

public class KnearestNeighbors implements RobotLearner {
	private HashMap<Move,ArrayList<AdaptedYUYVImage>> neighbors = new HashMap<>();
	private int k = 3;

	
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
		TreeMap<Long, Move> kSet = new TreeMap<>();
		for (Move m: neighbors.keySet()){
			for (AdaptedYUYVImage i: neighbors.get(m)){
				long tempDist = distFunction(i,img);
				if (kSet.size() < k){
					kSet.put(tempDist, m);
				}
				else if (tempDist < minDist){
					kSet.put(tempDist, m);
					kSet.pollLastEntry();
					minDist = kSet.lastKey();
				}
			}
		}
		
		return getMostCommonElement(kSet);
		
	}
	public long distFunction(AdaptedYUYVImage img1, AdaptedYUYVImage img2){
		// Function subject to change
		long dist = YUYVDistanceFuncs.euclideanAllChannels(img1, img2);
		return dist;
	}
	public Move getMostCommonElement(TreeMap<Long, Move> kClosest){
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
