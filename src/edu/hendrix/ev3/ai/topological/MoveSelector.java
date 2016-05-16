package edu.hendrix.ev3.ai.topological;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.DeepCopyable;
import edu.hendrix.ev3.util.Duple;

public class MoveSelector<T extends Clusterable<T> & DeepCopyable<T>> {
	private StateMap<T> map;
	private MoveSet choices;
	private Move mostRecent;
	private int currentState, exploreConstant;
	
	public MoveSelector(Function<Integer,StateMap<T>> mapMaker, int numNodes, int exploreConstant, MoveSet choices) {
		map = mapMaker.apply(numNodes);
		currentState = map.getStartingLabel();
		this.exploreConstant = exploreConstant;
		this.choices = choices;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("{");
		result.append(map.toString());
		result.append("}{");
		result.append(choices.toString());
		result.append("}{");
		result.append(mostRecent.toString());
		result.append("}{");
		result.append(currentState);
		result.append("}{");
		result.append(exploreConstant);
		result.append("}");
		return result.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		return this.toString().equals(other.toString());
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
	
	public double chanceToExplore() {
		int count = map.numCountsFor(currentState, mostRecent) 
				- getMostPopular().getSecond() 
				+ getLeastPopular().getSecond();
		return (double)exploreConstant / (exploreConstant + count);
	}
	
	public void train(Move currentMove, T currentSensors) {
		currentState = map.addTransition(currentState, currentMove, currentSensors);
		mostRecent = currentMove;
	}
	
	public Path pathTo(T currentSensors, Predicate<T> goal) {
		PathsFrom from = map.bestPathsFrom(currentSensors);
		if (from.getNumDestinations() > 0) {
			return from.bestPathTo(map.acceptableNodes(goal));
		} else {
			return new Path();
		}
	}
	
	public Move getExploringMove() {
		return getLeastPopular().getFirst();
	}
	
	public Move getMostRecentMove() {
		return mostRecent;
	}
	
	public int getCurrentState() {
		return currentState;
	}
	
	public Duple<Move,Integer> getMostPopular() {
		return pickMoveFromCount(Integer.MIN_VALUE, (upstart, current) -> upstart > current);
	}
	
	public Duple<Move,Integer> getLeastPopular() {
		return pickMoveFromCount(Integer.MAX_VALUE, (upstart, current) -> upstart < current);
	}
	
	public Duple<Move,Integer> pickMoveFromCount(int startCount, BiPredicate<Integer,Integer> replacer) {
		Duple<Move,Integer> result = new Duple<>(Move.NONE, startCount);
		for (Move choice: choices) {
			int choiceCount = map.numCountsFor(currentState, choice);
			if (replacer.test(choiceCount, result.getSecond())) {
				result = new Duple<>(choice, choiceCount);
			}
		}
		return result;
	}
}
