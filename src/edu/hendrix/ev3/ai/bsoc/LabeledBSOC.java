package edu.hendrix.ev3.ai.bsoc;

import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.ai.cluster.DistanceFunc;
import edu.hendrix.ev3.util.DeepCopyable;
import edu.hendrix.ev3.util.EnumHistogram;

import java.util.Map.Entry;
import java.util.TreeMap;

public class LabeledBSOC<T extends Clusterable<T> & DeepCopyable<T>, E extends Enum<E>> {
	private BoundedSelfOrgCluster<T> bsoc;
	private TreeMap<Integer,EnumHistogram<E>> node2counts;
	private Class<E> enumClass;
	private E unclassifiable;
	
	public LabeledBSOC(Class<E> enumClass, int maxNumNodes, DistanceFunc<T> dist, E unclassifiableLabel) {
		this.enumClass = enumClass;
		bsoc = new BoundedSelfOrgCluster<>(maxNumNodes, dist);
		node2counts = new TreeMap<>();
		this.unclassifiable = unclassifiableLabel;
		bsoc.addListener(new BSOCListener(){
			@Override
			public void addingNode(int node) {
				addHistogram(node);
			}
			@Override
			public void replacingNode(int target, int replacement) {
				for (Entry<E, Integer> label: node2counts.get(target)) {
					EnumHistogram<E> toUpdate = node2counts.get(replacement);
					toUpdate.setCountFor(label.getKey(), label.getValue() + toUpdate.getCountFor(label.getKey()));
				}
				node2counts.remove(target);
			}});
	}
	
	private void addHistogram(int node) {
		node2counts.put(node, new EnumHistogram<>(enumClass));
	}
	
	public void train(T example, E label) {
		int node = bsoc.train(example);
		node2counts.get(node).bump(label);
	}
	
	public boolean isTrained() {
		return bsoc.size() > 0;
	}
	
	public int size() {return bsoc.size();}
	
	public E bestMatchFor(T example) {
		EnumHistogram<E> counts = getCountsFor(example);
		if (counts.getNumKeys() == 1) {
			return counts.getHighestCounted();
		} else {
			return unclassifiable;
		}
	}
	
	public EnumHistogram<E> getCountsFor(T example) {
		int node = bsoc.getClosestMatchFor(example);
		return node2counts.get(node).deepCopy();
	}
}
