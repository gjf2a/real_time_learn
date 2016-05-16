package edu.hendrix.ev3.ai.topological;

import java.util.function.Function;
import java.util.function.Predicate;

import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.DeepCopyable;

// This class is designed to support a controller that "patiently" sticks
// with a particular move for a while, and only changes moves when it appears
// that maintaining the current move is futile.
//
// Because of the "futility" concept, the idea of exploration is employed 
// when the controller runs out of "patience".  This gives a very different
// feel from the original MoveSelector.

public class PatientMoveSelector<T extends Clusterable<T> & DeepCopyable<T>> {
	private StateMap<T> map;
	private MoveSet choices;
	private Move mostRecent;
	private int currentState, cyclesRemaining;
	
	public PatientMoveSelector(Function<Integer,StateMap<T>> mapMaker, int numNodes, MoveSet choices){
		this.map = mapMaker.apply(numNodes);
		this.currentState = map.getStartingLabel();
		this.choices = choices;
		this.cyclesRemaining = 0;
		this.mostRecent = Move.NONE;
	}

	public static int estimatedSteps(double probSuccess, double minProb) {
		int steps = 0;
		double probFailure = 1.0 - probSuccess;
		double failureEstimate = probFailure;
		double maxProb = 1.0 - minProb;
		while (failureEstimate >= maxProb) {
			steps += 1;
			failureEstimate *= probFailure;
		}
		return steps;
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
		result.append(cyclesRemaining);
		result.append("}");
		return result.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		return this.toString().equals(other.toString());
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
	
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
	
	public Move getMostRecentMove() {
		return mostRecent;
	}
	
	public int getCurrentState() {
		return currentState;
	}
	
	public int getCyclesRemaining() {
		return cyclesRemaining;
	}
}
