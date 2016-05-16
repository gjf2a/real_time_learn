package edu.hendrix.ev3.ai.topological;

import java.util.TreeSet;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.ai.cluster.DistanceFunc;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.DeepCopyable;
import edu.hendrix.ev3.util.Util;

public class HierarchicalStateMap<T extends Clusterable<T> & DeepCopyable<T>> implements DeepCopyable<HierarchicalStateMap<T>>{
	private ArrayList<StateMap<T>> hierarchy;
	private ArrayList<TreeMap<Integer,TreeSet<Integer>>> level2mappings;
	
	private HierarchicalStateMap() {
		hierarchy = new ArrayList<>();
		level2mappings = new ArrayList<>();
	}
	
	private void addStateMap(StateMap<T> map) {
		hierarchy.add(map);
		level2mappings.add(new TreeMap<>());
	}
	
	public HierarchicalStateMap(int maxNodes, DistanceFunc<T> dist) {
		this();
		int numLevels = Util.log2Floor(maxNodes);
		for (int i = 0; i < numLevels; i++) {
			addStateMap(new StateMap<>(maxNodes, dist));
			maxNodes /= 2;
		}
	}

	@Override
	public HierarchicalStateMap<T> deepCopy() {
		HierarchicalStateMap<T> result = new HierarchicalStateMap<>();
		DeepCopyable.copyFromInto(this.hierarchy, result.hierarchy);
		DeepCopyable.copyFromInto(this.level2mappings, result.level2mappings, map -> {
			TreeMap<Integer,TreeSet<Integer>> mappings = new TreeMap<>();
			DeepCopyable.copyFromInto(map, mappings, treeSet -> new TreeSet<>(treeSet));
			return mappings;
		});
		return result;
	}
	
	public int getNumLevels() {return hierarchy.size();}
	
	public TreeSet<Integer> getChildrenBelow(int level, int parentNode) {
		return level2mappings.get(level).get(parentNode);
	}
	
	public TreeSet<Integer> getDescendantsAt(int sourceLevel, int sourceNode) {
		TreeSet<Integer> sources = null;
		TreeSet<Integer> result = new TreeSet<>();
		result.add(sourceNode);
		for (int level = sourceLevel; level > 0; level--) {
			sources = result;
			result = new TreeSet<>();
			for (int src: sources) {
				result.addAll(level2mappings.get(level).get(src));
			}
		}
		return result;
	}
	
	public void visitLevelBelow(int level, int node, java.util.function.Consumer<T> visitor) {
		int below = level - 1;
		for (int belowNode: level2mappings.get(level).get(node)) {
			visitor.accept(hierarchy.get(below).getIdealInputFor(belowNode));
		}
	}
	
	// if constraint(input) is true, returns the highest level. 
	// Otherwise, returns the highest level at which:
	// - constraint(best node for input) is false
	// - There exists some other node for which constraint is true.
	public int highestSeparableLevel(T input, Predicate<T> constraint) {
		if (constraint.test(input)) {
			return getNumLevels() - 1;
		} else {
			for (int level = getNumLevels() - 1; level > 0; level--) {
				int best = hierarchy.get(level).getStateFor(input);
				T ideal = hierarchy.get(level).getIdealInputFor(best);
				if (!constraint.test(ideal)) {
					for (int node: hierarchy.get(level).allNodes()) {
						if (node != best) {
							T nodeIdeal = hierarchy.get(level).getIdealInputFor(node);
							if (constraint.test(nodeIdeal)) {
								return level;
							}
						}
					}
				}
			}
			return 0;
		}
	}
	
	public int bestLevelFor(Predicate<T> constraint) {
		for (int level = getNumLevels() - 1; level > 0; level--) {
			if (allNodesHappy(constraint, level)) {
				return level;
			}
		}
		return 0;
	}
	
	private boolean allNodesHappy(Predicate<T> constraint, int level) {
		for (int node: hierarchy.get(level).allNodes()) {
			if (!allMatch(constraint.test(hierarchy.get(level).getIdealInputFor(node)), getDescendantsAt(level, node), constraint)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean allMatch(boolean targetResult, Iterable<Integer> nodes, Predicate<T> constraint) {
		for (int node: nodes) {
			if (constraint.test(hierarchy.get(0).getIdealInputFor(node)) != targetResult) {
				return false;
			}
		}
		return true;
	}
	
	public int[] addTransition(int[] currentState, Move transition, T currentInput) {
		Util.assertArgument(currentState.length == getNumLevels(), String.format("currentState needs %d elements, not %d", getNumLevels(), currentState.length));
		int[] updatedState = applyToAllIndices(i -> hierarchy.get(i).addTransition(currentState[i], transition, currentInput));
		resetMappings();
		return updatedState;
	}
	
	private void resetMappings() {
		for (TreeMap<Integer,TreeSet<Integer>> mapping: level2mappings) {
			mapping.clear();
		}
		for (int level = 1; level < hierarchy.size(); level++) {
			for (int node: hierarchy.get(level).allNodes()) {
				level2mappings.get(level).put(node, new TreeSet<Integer>());
			}
		}
		for (int level = 0; level < hierarchy.size() - 1; level++) {
			StateMap<T> upMap = hierarchy.get(level + 1);
			TreeMap<Integer,TreeSet<Integer>> upMapping = level2mappings.get(level + 1);
			for (int node: hierarchy.get(level).allNodes()) {
				int bestUp = upMap.getStateFor(hierarchy.get(level).getIdealInputFor(node));
				upMapping.get(bestUp).add(node);
			}
		}
	}
	
	public int[] getStateFor(T input) {
		return applyToAll(map -> map.getStateFor(input));
	}
	
	public int[] getStartingLabel() {
		return applyToAll(map -> map.getStartingLabel());
	}
	
	public int[] applyToAll(Function<StateMap<T>, Integer> func) {
		return applyToAllIndices(i -> func.apply(hierarchy.get(i)));
	}
	
	private int[] applyToAllIndices(Function<Integer,Integer> func) {
		int[] result = new int[getNumLevels()];
		for (int i = 0; i < getNumLevels(); i++) {
			result[i] = func.apply(i);
		}
		return result;
	}
	
	public ArrayList<Integer> allNodesFor(int level) {
		return hierarchy.get(level).allNodes();
	}
	
	public T getIdealInputFor(int level, int state) {
		return hierarchy.get(level).getIdealInputFor(state);
	}
	
	public int totalOutgoingFor(int level, int state) {
		return hierarchy.get(level).numCountsFor(state);
	}
	
	public static <T extends Clusterable<T> & DeepCopyable<T>> HierarchicalStateMap<T> fromString(String src, Function<String,T> extractor, DistanceFunc<T> dist) {
		HierarchicalStateMap<T> result = new HierarchicalStateMap<>();
		for (String levelStr: Util.debrace(src)) {
			ArrayList<String> levelParts = Util.debrace(levelStr);
			result.addStateMap(new StateMap<>(levelParts.get(0), extractor, dist));
			if (levelParts.size() > 1) {
				TreeMap<Integer,TreeSet<Integer>> mappings = new TreeMap<>();
				ArrayList<String> mappingParts = Util.debrace(levelParts.get(1));
				for (int j = 0; j < mappingParts.size(); j += 2) {
					TreeSet<Integer> targets = new TreeSet<>();
					for (String target: mappingParts.get(j+1).split(",")) {
						if (target.length() > 0) {
							targets.add(Integer.parseInt(target));
						}
					}
					mappings.put(Integer.parseInt(mappingParts.get(j)), targets);
				}
				result.level2mappings.set(result.level2mappings.size() - 1, mappings);
			} 
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int level = 0; level < hierarchy.size(); level++) {
			result.append("{{");
			result.append(hierarchy.get(level).toString());
			result.append("}\n{");
			for (Entry<Integer, TreeSet<Integer>> mapping: level2mappings.get(level).entrySet()) {
				result.append(mapping.getKey());
				result.append('{');
				for (int target: mapping.getValue()) {
					result.append(target);
					result.append(',');
				}
				result.append('}');
			}
			result.append("}}\n");
		}
		return result.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		return toString().equals(other.toString());
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
}
