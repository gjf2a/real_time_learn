package edu.hendrix.ev3.ai.cluster.visualize;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.ai.cluster.Clusterer;
import edu.hendrix.ev3.ai.cluster.DistanceFunc;

public class ClusterAssessor<T extends Clusterable<T>> {
	private ArrayList<T> inputs;
	private Clusterer<T> clusterer;
	
	public ClusterAssessor(ArrayList<T> inputs, Clusterer<T> clusterer) {
		this.inputs = inputs;
		this.clusterer = clusterer;
		for (T input: inputs) {
			clusterer.train(input);
		}
	}
	
	public int getNumInputs() {return inputs.size();}
	
	public double ssdInputs2nodes(DistanceFunc<T> distFunc) {
		return processDiffs(distFunc, () -> 0.0, (ssd, d) -> ssd + d);
	}
	
	public ArrayList<Long> squaredDiffs(DistanceFunc<T> distFunc) {
		return processDiffs(distFunc, () -> new ArrayList<>(), (xs, d) -> {xs.add(d); return xs;});
	}
	
	public static void printAll(ArrayList<Long> inputs2values) {
		for (int i = 0; i < inputs2values.size(); i++) {
			System.out.println(i + "," + inputs2values.get(i));
		}
	}
	
	private <R> R processDiffs(DistanceFunc<T> distFunc, Supplier<R> initializer, BiFunction<R,Long,R> folder) {
		R result = initializer.get();
		for (T input: inputs) {
			int state = clusterer.getClosestMatchFor(input);
			long d = distFunc.distance(input, clusterer.getIdealInputFor(state));
			result = folder.apply(result, d*d);
		}
		return result;
	}
	
	public double ssdNodes2Nodes(DistanceFunc<T> distFunc) {
		double ssd = 0;
		for (int node1: clusterer.getClusterIds()) {
			for (int node2: clusterer.getClusterIds()) {
				if (node1 != node2) {
					long d = distFunc.distance(clusterer.getIdealInputFor(node1), clusterer.getIdealInputFor(node2));
					ssd += d*d;
				}
			}
		}
		return ssd;
	}
}
