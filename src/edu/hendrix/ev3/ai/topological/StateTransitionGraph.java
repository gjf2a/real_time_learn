package edu.hendrix.ev3.ai.topological;

import java.util.TreeSet;
import java.util.function.Function;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map.Entry;

import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.DeepCopyable;
import edu.hendrix.ev3.util.Util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.TreeMap;
import java.util.HashMap;

public class StateTransitionGraph implements DeepCopyable<StateTransitionGraph> {
	private TreeMap<Integer,StateTransitionNode> nodes;
	private TreeMap<Integer,TreeSet<Integer>> sourcesFor;
	
	public StateTransitionGraph() {
		nodes = new TreeMap<>();
		sourcesFor = new TreeMap<>();
	}

	@Override
	public StateTransitionGraph deepCopy() {
		StateTransitionGraph result = new StateTransitionGraph();
		DeepCopyable.copyFromInto(this.nodes, result.nodes);
		DeepCopyable.copyFromInto(this.sourcesFor, result.sourcesFor, treeSet -> new TreeSet<>(treeSet));
		return result;
	}
	
	public StateTransitionGraph(String src) {
		this();
		ArrayList<String> parts = Util.debrace(src);
		for (String nodeStr: Util.debrace(parts.get(0))) {
			StateTransitionNode node = new StateTransitionNode(nodeStr);
			nodes.put(node.getID(), node);
		}
		for (String sourceStr: Util.debrace(parts.get(1))) {
			ArrayList<String> srcParts = Util.debrace(sourceStr);
			TreeSet<Integer> srcs = new TreeSet<>();
			sourcesFor.put(Integer.parseInt(srcParts.get(0)), srcs);
			if (srcParts.size() > 1) {
				for (String srcFor: srcParts.get(1).split(";")) {
					srcs.add(Integer.parseInt(srcFor));
				}
			}
		}
		assertInvariant();
	}
	
	@Override
	public boolean equals(Object other) {
		return toString().equals(other.toString());
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('{');
		for (Entry<Integer, StateTransitionNode> node: nodes.entrySet()) {
			result.append('{');
			result.append(node.getValue().toString());
			result.append('}');
		}
		result.append("}{");
		for (Entry<Integer, TreeSet<Integer>> sources: sourcesFor.entrySet()) {
			result.append("{");
			result.append(sources.getKey());
			result.append("{");
			for (int source: sources.getValue()) {
				result.append(source);
				result.append(';');
			}
			result.append("}}");
		}
		result.append('}');
		return result.toString();
	}
	
	public PathsFrom bestPathsFrom(int node) {
		PathsFrom paths = new PathsFrom(node);
		HashSet<Integer> unvisited = initUnvisitedNodes(node);
		PriorityQueue<Path> candidates = makeHeap();
		addSuccessorsTo(candidates, new Path(paths.getSource()));
		while (!candidates.isEmpty() && !unvisited.isEmpty()) { 
			Path candidate = candidates.remove();
			if (unvisited.contains(candidate.getDestination())) {
				unvisited.remove(candidate.getDestination());
				paths.addPath(candidate);
				addSuccessorsTo(candidates, candidate);
			}
		} 
		return paths;
	}
	
	private HashSet<Integer> initUnvisitedNodes(int startNode) {
		HashSet<Integer> unvisited = new HashSet<>();
		unvisited.addAll(nodes.keySet());
		unvisited.remove(startNode);
		return unvisited;
	}
	
	private PriorityQueue<Path> makeHeap() {
		return new PriorityQueue<>((path1, path2) -> {
			double diff = path1.getProbability() - path2.getProbability(); 
			return diff < 0 ? 1 : diff > 0 ? -1 : 0;});
	}
	
	private void addSuccessorsTo(PriorityQueue<Path> heap, Path src) {
		int at = src.getDestination();
		for (Move move: movesFrom(at)) {
			for (Entry<Integer, Double> edge: successorsTo(at, move).entrySet()) {
				heap.add(src.createSuccessor(move, edge.getKey(), edge.getValue()));
			}
		}
	}
	
	public Set<Integer> getAllNodes() {
		return nodes.keySet();
	}
	
	public EnumSet<Move> movesFrom(int node) {
		Util.assertArgument(nodes.containsKey(node), String.format("Node %d not present", node));
		return nodes.get(node).getOutgoingMoves();
	}
	
	public int getTotalCount() {
		int count = 0;
		for (int node: getAllNodes()) {
			count += getCountFrom(node);
		}
		return count;
	}
	
	private int getSafeCount(Function<StateTransitionNode,Integer> getter, int node) {
		return hasNode(node) ? getter.apply(nodes.get(node)) : 0;
	}
	
	public int getCountFrom(int node) {
		return getSafeCount(n -> n.getOutgoingCount(), node);
	}
	
	public int getCountForMoveFrom(int node, Move move) {
		return getSafeCount(n -> n.getTotalOutgoingFor(move), node);
	}
	
	public Set<Integer> getSuccessorIds(int node, Move move) {
		return hasNode(node) ? nodes.get(node).getSuccessorsFor(move) : new TreeSet<>();
	}
	
	public int getCountToSuccessor(int node, Move move, int successor) {
		return getSafeCount(n -> n.getCountFor(move, successor), node);
	}
	
	public HashMap<Integer,Double> successorsTo(int node, Move move) {
		return hasNode(node) ? nodes.get(node).successorsTo(move) : new HashMap<>();
	}
	
	public boolean hasNode(int node) {
		return nodes.containsKey(node);
	}

	public void addNode(int node) {
		Util.assertArgument(!nodes.containsKey(node), String.format("Node %d already exists", node));
		nodes.put(node, new StateTransitionNode(node));
		sourcesFor.put(node, new TreeSet<>());
	}
	
	public void purgeAndMerge(int purge, int mergeWith) {
		Util.assertArgument(nodes.containsKey(purge), String.format("purging %d: not a live node", purge));
		Util.assertArgument(nodes.containsKey(mergeWith), String.format("merge with %d: not a live node", mergeWith));
		StateTransitionNode purged = nodes.remove(purge);
		nodes.get(mergeWith).absorbOutgoingFrom(purged);
		renumberIncomingEdges(purge, mergeWith);
		redirectReferences(purged, mergeWith);
		sourcesFor.remove(purge);
		assertInvariant(); // TODO: very expensive check; comment it out when done debugging
	}
	
	public void purge(int purge) {
		Util.assertArgument(nodes.containsKey(purge), String.format("purging %d: not a live node", purge));
		StateTransitionNode purged = nodes.remove(purge);
		removeReferences(purged);
		sourcesFor.remove(purge);
		assertInvariant();
	}
	
	private void removeReferences(StateTransitionNode purged) {
		for (Move move: purged.getOutgoingMoves()) {
			for (int referenced: purged.getSuccessorsFor(move)) {
				sourcesFor.get(referenced).remove(purged.getID());
			}
		}	
	}
	
	private void renumberIncomingEdges(int purge, int mergeWith) {
		for (int referencer: sourcesFor.get(purge)) {
			if (nodes.containsKey(referencer)) {
				nodes.get(referencer).renumber(purge, mergeWith);
			}
			int updated = referencer == purge ? mergeWith : referencer;
			sourcesFor.get(mergeWith).add(updated);
		}		
	}
	
	private void redirectReferences(StateTransitionNode purged, int mergeWith) {
		for (Move move: purged.getOutgoingMoves()) {
			for (int referenced: purged.getSuccessorsFor(move)) {
				sourcesFor.get(referenced).remove(purged.getID());
				sourcesFor.get(referenced).add(mergeWith);
			}
		}		
	}
	
	public void assertInvariant() {
		for (int node: nodes.keySet()) {
			Util.assertState(sourcesFor.containsKey(node), String.format("Node %d missing from sourcesFor", node));
		}
		
		for (int node: sourcesFor.keySet()) {
			Util.assertState(nodes.containsKey(node), String.format("Node %d missing from nodes", node));
			for (int src: sourcesFor.get(node)) {
				Util.assertState(nodes.containsKey(src), String.format("%d in sourcesFor (%d) but not nodes", src, node));
				Util.assertState(nodes.get(src).hasReferenceTo(node), String.format("Node %d claims false reference by %d", node, src));
			}
		}
		
		for (Entry<Integer, StateTransitionNode> node: nodes.entrySet()) {
			Util.assertState(node.getKey().equals(node.getValue().getID()), "ID mismatch");
			for (Move outgoing: node.getValue().getOutgoingMoves()) {
				for (int successor: node.getValue().getSuccessorsFor(outgoing)) {
					Util.assertState(sourcesFor.get(successor).contains(node.getKey()), String.format("Missing source %d for node %d", node.getKey(), successor));
				}
			}
		}
	}
	
	public void addTransition(int start, Move transition, int end) {
		Util.assertArgument(nodes.containsKey(start), String.format("Start node %d does not exist", start));
		Util.assertArgument(nodes.containsKey(end), String.format("End node %d does not exist", end));
		nodes.get(start).train(transition, end);
		sourcesFor.get(end).add(start);
	}
}
