package edu.hendrix.ev3.ai.supervised;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import edu.hendrix.ev3.ai.cluster.Clusterer;
import edu.hendrix.ev3.ai.cluster.DistanceFunc;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.Triple;
import edu.hendrix.ev3.util.Util;

public class SupervisedClusterer<T, L extends Enum<L>> {
	private EnumMap<L,Clusterer<T>> label2cluster;
	private Function<DistanceFunc<T>,Clusterer<T>> clusterMaker;
	private DistanceFunc<T> func;
	private Class<L> labelClass;
	
	public SupervisedClusterer(Class<L> labelClass, Function<DistanceFunc<T>,Clusterer<T>> clusterMaker, DistanceFunc<T> func) {
		this.clusterMaker = clusterMaker;
		this.func = func;
		this.labelClass = labelClass;
		label2cluster = new EnumMap<>(labelClass);
	}
	
	public SupervisedClusterer(String src, Function<String,L> labelMaker, Function<String,Clusterer<T>> clusterExtractor, Class<L> labelClass, Function<DistanceFunc<T>,Clusterer<T>> clusterMaker, DistanceFunc<T> func) {
		this(labelClass, clusterMaker, func);
		for (String pair: Util.debrace(src)) {
			ArrayList<String> parts = Util.debrace(pair);
			label2cluster.put(labelMaker.apply(parts.get(0)), clusterExtractor.apply(parts.get(1)));
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Entry<L, Clusterer<T>> label: label2cluster.entrySet()) {
			result.append("{{");
			result.append(label.getKey());
			result.append("}{");
			result.append(label.getValue());
			result.append("}}");
		}
		return result.toString();
	}
	
	public EnumMap<L,TreeSet<Integer>> getBorderNodes() {
		EnumMap<L,TreeSet<Integer>> result = new EnumMap<>(labelClass);
		for (Entry<L, Clusterer<T>> entry: label2cluster.entrySet()) {
			for (int node: entry.getValue().getClusterIds()) {
				if (isBorderNode(entry.getKey(), node)) {
					if (!result.containsKey(entry.getKey())) {
						result.put(entry.getKey(), new TreeSet<>());
					}
					result.get(entry.getKey()).add(node);
				}
			}
		}
		return result;
	}
	
	public DistanceFunc<T> getDistanceFunc() {return func;}
	
	public boolean isBorderNode(L label, int node) {
		Clusterer<T> cluster = label2cluster.get(label);
		T nodeIdealInput = cluster.getIdealInputFor(node);
		long closestSame = cluster.getNodeRanking(nodeIdealInput).get(1).getSecond();
		for (Entry<L,Clusterer<T>> otherEntry: label2cluster.entrySet()) {
			long closestOther = otherEntry.getValue().getNodeRanking(nodeIdealInput).get(0).getSecond();
			if (otherEntry.getKey() != label && closestSame > closestOther) {
				return true;
			}
		}
		return false;
	}
	
	public int size() {
		int total = 0;
		for (Entry<L, Clusterer<T>> label: label2cluster.entrySet()) {
			total += label.getValue().size();
		}
		return total;
	}
	
	public void train(T example, L label) {
		if (!label2cluster.containsKey(label)) {
			label2cluster.put(label, clusterMaker.apply(func));
		}
		label2cluster.get(label).train(example);
	}
	
	public Triple<L,Integer,Long> bestMatchNodeDistanceFor(T example) {
		Triple<L,Integer,Long> result = null;
		for (Entry<L, Clusterer<T>> entry: label2cluster.entrySet()) {
			if (entry.getValue().size() > 0) {
				Duple<Integer,Long> ranking = entry.getValue().getClosestNodeDistanceFor(example);
				long labelDist = ranking.getSecond();
				if (result == null || labelDist < result.getThird()) {
					result = new Triple<>(entry.getKey(), ranking.getFirst(), labelDist);
				}
			}
		}
		return result;
	}
	
	public ArrayList<Triple<L,Integer,Long>> totalNodeRankingFor(T example) {
		ArrayList<Triple<L,Integer,Long>> result = new ArrayList<>();
		for (Entry<L, Clusterer<T>> entry: label2cluster.entrySet()) {
			for (Duple<Integer,Long> node: entry.getValue().getNodeRanking(example)) {
				result.add(new Triple<>(entry.getKey(), node.getFirst(), node.getSecond()));
			}
		}
		Collections.sort(result, (e1, e2) -> (int)(e1.getThird() - e2.getThird()));
		return result;
	}
	
	public boolean isTrained() {
		return label2cluster.size() > 0;
	}
	
	public T getIdealInputFor(L label, int node) {
		return label2cluster.get(label).getIdealInputFor(node);
	}

	public Set<L> allLabels() {return Collections.unmodifiableSet(label2cluster.keySet());}

	public int numClustersWith(L label) {
		return label2cluster.get(label).size();
	}

	public int bestRankedAlternative(T example) {
		EnumMap<L,ArrayList<Duple<Integer,Long>>> label2rankings = getLabelRankings(example);
		Duple<L,EnumMap<L,Long>> bests = getBestDistances(label2rankings);
		return findBestRank(label2rankings, bests);
	}
	
	public Collection<Integer> getClusterIdsFor(L label) {
		return label2cluster.get(label).getClusterIds();
	}
	
	private EnumMap<L,ArrayList<Duple<Integer,Long>>> getLabelRankings(T example) {
		EnumMap<L,ArrayList<Duple<Integer,Long>>> label2rankings = new EnumMap<>(labelClass);
		for (Entry<L, Clusterer<T>> ent: label2cluster.entrySet()) {
			label2rankings.put(ent.getKey(), ent.getValue().getNodeRanking(example));
		}	
		return label2rankings;
	}
	
	private Duple<L,EnumMap<L,Long>> getBestDistances(EnumMap<L,ArrayList<Duple<Integer,Long>>> label2rankings) {
		EnumMap<L,Long> bestDistances = new EnumMap<>(labelClass);
		L bestLabel = null;
		for (Entry<L, ArrayList<Duple<Integer,Long>>> m: label2rankings.entrySet()) {
			bestDistances.put(m.getKey(), m.getValue().get(0).getSecond());
			if (bestLabel == null || bestDistances.get(m.getKey()) < bestDistances.get(bestLabel)) {
				bestLabel = m.getKey();
			}
		}
		return new Duple<>(bestLabel, bestDistances);
	}
	
	private int findBestRank(EnumMap<L,ArrayList<Duple<Integer,Long>>> label2rankings, Duple<L,EnumMap<L,Long>> bests) { 
		L bestLabel = bests.getFirst();
		EnumMap<L,Long> bestDistances = bests.getSecond();
		int bestRank = 1;
		ArrayList<Duple<Integer,Long>> bestRanks = label2rankings.get(bestLabel);
		boolean found = false;
		while (!found && bestRank < bestRanks.size()) {
			long dist = bestRanks.get(bestRank).getSecond();
			for (Entry<L, Long> m: bestDistances.entrySet()) {
				if (m.getKey() != bestLabel && m.getValue() < dist) {
					found = true;
				}
				bestRank++;
			}
		}
		return bestRank;
	}
}
