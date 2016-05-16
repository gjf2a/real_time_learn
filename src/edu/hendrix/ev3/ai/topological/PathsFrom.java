package edu.hendrix.ev3.ai.topological;

import java.util.HashMap;

import edu.hendrix.ev3.util.Util;

public class PathsFrom {
	private int source;
	private HashMap<Integer,Path> pathsToDestinations;
	
	public PathsFrom(int source) {
		this.source = source;
		pathsToDestinations = new HashMap<>();
	}
	
	public int getNumDestinations() {return pathsToDestinations.size();}
	
	public void addPath(Path path) {
		if (path.getOrigin() != source) {
			throw new IllegalArgumentException("Wrong source: " + path.getOrigin() + " target source: " + source);
		}
		pathsToDestinations.put(path.getDestination(), path);
	}
	
	public int getSource() {return source;}
	
	public Path pathTo(int destination) {
		return pathsToDestinations.get(destination);
	}
	
	public Path bestPathTo(Iterable<Integer> candidates) {
		Util.assertState(getNumDestinations() > 0, "No paths exist");
		Path best = new Path();
		for (int candidate: candidates) {
			Path candidatePath = pathsToDestinations.get(candidate);
			if (candidatePath != null && candidatePath.getProbability() > best.getProbability()) {
				best = candidatePath;
			}
		}
		return best;
	}
}
