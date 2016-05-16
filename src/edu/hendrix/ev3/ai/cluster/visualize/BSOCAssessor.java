package edu.hendrix.ev3.ai.cluster.visualize;

import java.util.ArrayList;

import edu.hendrix.ev3.ai.bsoc.BoundedSelfOrgCluster;
import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.ai.cluster.DistanceFunc;
import edu.hendrix.ev3.util.DeepCopyable;

public class BSOCAssessor<T extends Clusterable<T> & DeepCopyable<T>> extends ClusterAssessor<T> {
	private DistanceFunc<T> func;
	
	public BSOCAssessor(ArrayList<T> inputs, DistanceFunc<T> func, int numNodes) {
		super(inputs, new BoundedSelfOrgCluster<T>(numNodes, func));
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
