package edu.hendrix.ev3.ai.topological;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import edu.hendrix.ev3.ai.bsoc.BSOCListener;
import edu.hendrix.ev3.ai.bsoc.BoundedSelfOrgCluster;
import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.ai.cluster.DistanceFunc;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.DeepCopyable;
import edu.hendrix.ev3.util.Util;

public class StateMap<T extends Clusterable<T> & DeepCopyable<T>> implements DeepCopyable<StateMap<T>> {
	private BoundedSelfOrgCluster<T> bsoc;
	private StateTransitionGraph graph;
	
	private class ReplacementAlerter implements BSOCListener {
		boolean replacing = false;
		int obsolete, replacement;
		
		void reset() {replacing = false;}

		@Override
		public void addingNode(int node) {}

		@Override
		public void replacingNode(int target, int replacement) {
			replacing = true;
			obsolete = target;
			this.replacement = replacement;
		}
	}
	private ReplacementAlerter alerter = new ReplacementAlerter();
	
	private StateMap(BoundedSelfOrgCluster<T> bsoc, StateTransitionGraph graph) {
		this.bsoc = bsoc;
		this.graph = graph;
	}
	
	public StateMap(int numStates, DistanceFunc<T> dist) {
		this(new BoundedSelfOrgCluster<>(numStates, dist), new StateTransitionGraph());
		bsoc.addListener(new BSOCListener(){
			public void addingNode(int node) {graph.addNode(node);}
			public void replacingNode(int node, int replacement) {
				graph.purgeAndMerge(node, replacement);
			}});
		bsoc.addListener(alerter);
	}
	
	public DistanceFunc<T> getDistanceFunc() {return bsoc.getDistanceFunc();}
	
	public void addBSOCListener(BSOCListener listener) {
		bsoc.addListener(listener);
	}
	
	public StateMap(String src, Function<String,T> extractor, DistanceFunc<T> dist) {
		ArrayList<String> parts = Util.debrace(src);
		bsoc = new BoundedSelfOrgCluster<>(parts.get(0), extractor, dist);
		graph = new StateTransitionGraph(parts.get(1));
	}
	
	public StateMap<T> deepCopy() {
		return new StateMap<>(bsoc.deepCopy(), graph.deepCopy());
	}
	
	public void assertInvariant() {
		for (int node: bsoc.getClusterIds()) {
			Util.assertState(graph.hasNode(node), String.format("Node %d present in BSOC, not in graph", node));
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('{');
		result.append(bsoc.toString());
		result.append("}\n{");
		result.append(graph.toString());
		result.append('}');
		return result.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		return toString().equals(other.toString());
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
	
	public int addTransition(int currentState, Move transition, T currentInput) {
		alerter.reset();
		int newState = bsoc.train(currentInput);
		if (alerter.replacing && currentState == alerter.obsolete) {
			currentState = alerter.replacement;
		}
		graph.addTransition(currentState, transition, newState);
		return newState;
	}
	
	public int getStateFor(T input) {
		return bsoc.getClosestMatchFor(input);
	}
	
	public int getStartingLabel() {
		return bsoc.getStartingLabel();
	}
	
	public ArrayList<Integer> allNodes() {return bsoc.getClusterIds();}
	
	public ArrayList<Integer> acceptableNodes(Predicate<T> criterion) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int node: allNodes()) {
			if (criterion.test(getIdealInputFor(node))) {
				result.add(node);
			}
		}
		return result;
	}
	
	public T getIdealInputFor(int node) {
		return bsoc.getIdealInputFor(node);
	}
	
	public EnumSet<Move> movesFrom(int node) {return graph.movesFrom(node);}
	
	public Set<Integer> successorsTo(int node, Move move) {
		return graph.getSuccessorIds(node, move);
	}
	
	public int numCounts() {
		return graph.getTotalCount();
	}
	
	public int numCountsFor(int node) {
		return graph.getCountFrom(node);
	}
	
	public int numCountsFor(int node, Move move) {
		return graph.getCountForMoveFrom(node, move);
	}
	
	public int numCountsTo(int node, Move move, int successor) {
		return graph.getCountToSuccessor(node, move, successor);
	}
	
	public PathsFrom bestPathsFrom(T current) {
		return graph.bestPathsFrom(bsoc.getClosestMatchFor(current));
	}
	
	public int size() {return bsoc.size();}
}
