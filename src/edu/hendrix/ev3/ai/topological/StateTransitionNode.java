package edu.hendrix.ev3.ai.topological;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;

import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.DeepCopyable;
import edu.hendrix.ev3.util.Util;

public class StateTransitionNode implements DeepCopyable<StateTransitionNode> {
	private int id;
	private EnumMap<Move,TreeMap<Integer,StateTransitionEdge>> outgoing;
	
	private StateTransitionNode() {
		outgoing = new EnumMap<>(Move.class);
	}

	@Override
	public StateTransitionNode deepCopy() {
		StateTransitionNode copy = new StateTransitionNode(id);
		DeepCopyable.copyFromInto(this.outgoing, copy.outgoing, map -> {
			TreeMap<Integer,StateTransitionEdge> moveCopy = new TreeMap<>();
			DeepCopyable.copyFromInto(map, moveCopy);
			return moveCopy;
		});
		return copy;
	} 
	
	public StateTransitionNode(int id) {
		this();
		this.id = id;
	}

	public StateTransitionNode(String src) {
		this();
		ArrayList<String> parts = Util.debrace(src);
		this.id = Integer.parseInt(parts.get(0));
		if (parts.size() > 1) {
			for (String moveStr: Util.debrace(parts.get(1))) {
				ArrayList<String> moveParts = Util.debrace(moveStr);
				TreeMap<Integer,StateTransitionEdge> edges = new TreeMap<>();
				outgoing.put(Move.valueOf(moveParts.get(0)), edges);
				for (String edgeStr: Util.debrace(moveParts.get(1))) {
					StateTransitionEdge edge = new StateTransitionEdge(edgeStr);
					edges.put(edge.getEnd(), edge);
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(id);
		result.append('{');
		for (Entry<Move, TreeMap<Integer, StateTransitionEdge>> moveEdges: outgoing.entrySet()) {
			result.append('{');
			result.append(moveEdges.getKey());
			result.append('{');
			for (Entry<Integer, StateTransitionEdge> edge: moveEdges.getValue().entrySet()) {
				result.append('{');
				result.append(edge.getValue());
				result.append('}');
			}
			result.append("}}");
		}
		result.append('}');
		return result.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		return toString().equals(other.toString());
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
	
	public int getID() {return id;}
	
	public boolean hasReferenceTo(int otherNode) {
		for (Move move: outgoing.keySet()) {
			if (outgoing.get(move).containsKey(otherNode)) {
				return true;
			}
		}
		return false;
	}
	
	public EnumSet<Move> getOutgoingMoves() {
		return outgoing.isEmpty() ? EnumSet.noneOf(Move.class) : EnumSet.copyOf(outgoing.keySet());
	}

	public int getTotalOutgoingFor(Move move) {
		int totalCounts = 0;
		if (outgoing.containsKey(move)) {
			for (Entry<Integer, StateTransitionEdge> successor: outgoing.get(move).entrySet()) {
				totalCounts += successor.getValue().getCount();
			}
		}
		return totalCounts;
	}
	
	public int getOutgoingCount() {
		int count = 0;
		for (Move move: getOutgoingMoves()) {
			count += getTotalOutgoingFor(move);
		}
		return count;
	}
	
	public Set<Integer> getSuccessorsFor(Move move) {
		return outgoing.get(move).keySet();
	}
	
	public int getCountFor(Move move, int successor) {
		return outgoing.containsKey(move)
				? outgoing.get(move).containsKey(successor) 
						? outgoing.get(move).get(successor).getCount()
						: 0
				: 0;
	}
	
	public HashMap<Integer,Double> successorsTo(Move move) {
		int totalCounts = getTotalOutgoingFor(move);
		HashMap<Integer,Double> result = new HashMap<>();
		for (Entry<Integer, StateTransitionEdge> successor: outgoing.get(move).entrySet()) {
			result.put(successor.getKey(), ((double)successor.getValue().getCount()) / totalCounts);
		}
		return result;
	}
	
	public void train(Move transition, int destination) {
		if (!outgoing.containsKey(transition)) {
			outgoing.put(transition, new TreeMap<>());
		}
		if (!outgoing.get(transition).containsKey(destination)) {
			outgoing.get(transition).put(destination, new StateTransitionEdge(id, destination));
		}
		outgoing.get(transition).get(destination).bump();
	}
	
	public void absorbOutgoingFrom(StateTransitionNode other) {
		absorbOutgoingEdges(other);
		renumber(other.getID(), this.getID());
	}
	
	private void absorbOutgoingEdges(StateTransitionNode other) {
		for (Move move: other.outgoing.keySet()) {
			if (!outgoing.containsKey(move)) {
				outgoing.put(move, new TreeMap<>());
			}
			for (int target: other.outgoing.get(move).keySet()) {
				mergeEdges(move, other.outgoing.get(move).get(target), target);
			}
		}		
	}
	
	public void renumber(int oldNode, int newNode) {
		for (Move move: outgoing.keySet()) {
			TreeMap<Integer,StateTransitionEdge> nodes = outgoing.get(move);
			if (nodes.containsKey(oldNode)) {
				mergeEdges(move, outgoing.get(move).get(oldNode), newNode);
				nodes.remove(oldNode);
			}
		}
	}

	private void mergeEdges(Move move, StateTransitionEdge oldEdge, int newNode) {
		TreeMap<Integer,StateTransitionEdge> nodes = outgoing.get(move);
		if (nodes.containsKey(newNode)) {
			nodes.get(newNode).absorb(oldEdge);
		} else {
			StateTransitionEdge edge = new StateTransitionEdge(id, newNode);
			edge.absorb(oldEdge);
			nodes.put(newNode, edge);
		}
	}
}
