package edu.hendrix.ev3.ai.cluster.visualize;

import java.util.ArrayList;

import edu.hendrix.ev3.ai.bsoc.TreeBSOC;
import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.ai.cluster.DistanceFunc;
import edu.hendrix.ev3.util.DeepCopyable;

public class TreeBSOCAssessor<T extends Clusterable<T> & DeepCopyable<T>> extends ClusterAssessor<T> {
	private DistanceFunc<T> func;
	
	public TreeBSOCAssessor(ArrayList<T> inputs, DistanceFunc<T> func, int levels, int degree) {
		super(inputs, new TreeBSOC<T>(levels, degree, func));
		this.func = func;
	}
	
	public double ssdInputs2nodes() {
		return super.ssdInputs2nodes(func);
	}
	
	public double ssdNodes2Nodes() {
		return super.ssdNodes2Nodes(func);
	}
	
	public ArrayList<Long> squaredDiffs() {
		return super.squaredDiffs(func);
	}
}
