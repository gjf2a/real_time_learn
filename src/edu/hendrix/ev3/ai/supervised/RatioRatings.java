package edu.hendrix.ev3.ai.supervised;

import java.util.EnumMap;
import java.util.TreeMap;

import edu.hendrix.ev3.util.Duple;

public class RatioRatings<L extends Enum<L>> {
	private EnumMap<L,TreeMap<Integer,Duple<Integer,Integer>>> nodeRatings;
	
	public RatioRatings(Class<L> labelClass) {
		nodeRatings = new EnumMap<>(labelClass);
	}
	
	public Duple<Integer,Integer> getRatingFor(L label, int node) {
		if (nodeRatings.containsKey(label) && nodeRatings.get(label).containsKey(node)) {
			return nodeRatings.get(label).get(node);
		} else {
			return new Duple<>(0, 0);
		}
	}
	
	public int getCountFor(L label, int node) {
		return getRatingFor(label, node).getSecond();
	}
	
	public double getRatioFor(L label, int node) {
		Duple<Integer,Integer> rating = getRatingFor(label, node);
		return (double)rating.getFirst() / rating.getSecond();
	}
	
	public void rateNodeWith(Duple<L,Integer> bestMatch, L label) {
		if (!nodeRatings.containsKey(bestMatch.getFirst())) {
			nodeRatings.put(bestMatch.getFirst(), new TreeMap<>());
		}
		TreeMap<Integer,Duple<Integer,Integer>> ratingMap = nodeRatings.get(bestMatch.getFirst());
		Duple<Integer,Integer> current = ratingMap.containsKey(bestMatch.getSecond()) ? ratingMap.get(bestMatch.getSecond()) : new Duple<>(0, 0);
		Duple<Integer,Integer> updated = new Duple<>(current.getFirst() + (bestMatch.getFirst() == label ? 1 : 0), current.getSecond() + 1);
		ratingMap.put(bestMatch.getSecond(), updated);		
	}
}
