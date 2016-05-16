package edu.hendrix.ev3.ai.topological;

import java.util.ArrayList;

import edu.hendrix.ev3.remote.Move;

public class Path {
	private ArrayList<Move> moves;
	private ArrayList<Integer> states;
	private double prob;
	
	public Path() {
		moves = new ArrayList<>();
		states = new ArrayList<>();
		prob = 0.0;
	}
	
	public Path(int origin) {
		this();
		states.add(origin);
		prob = 1.0;
	}
	
	public Path(Path other) {
		this(other.getOrigin());
		for (int i = 0; i < other.moves.size(); i++) {
			addStep(other.moves.get(i), other.states.get(i+1), 1.0);
		}
		this.prob = other.prob;
	}
	
	private void addStep(Move move, int destination, double prob) {
		moves.add(move);
		states.add(destination);
		this.prob *= prob;
	}
	
	public Path createSuccessor(Move move, int destination, double prob) {
		Path result = new Path(this);
		result.addStep(move, destination, prob);
		return result;
	}
	
	public boolean exists() {return moves.size() > 0;}
	
	public double getProbability() {return prob;}
	
	public int getNumMoves() {return moves.size();}
	
	public Move getFirstMove() {return moves.get(0);}
	
	public int getOrigin() {return states.get(0);}
	
	public int getDestination() {return states.get(states.size() - 1);}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getOrigin());
		for (int i = 0; i < moves.size(); i++) {
			result.append(';');
			result.append(moves.get(i));
			result.append(':');
			result.append(states.get(i+1));
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
