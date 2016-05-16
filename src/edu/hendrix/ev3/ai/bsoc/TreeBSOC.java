package edu.hendrix.ev3.ai.bsoc;

import java.util.ArrayList;
import java.util.function.BiFunction;

import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.ai.cluster.Clusterer;
import edu.hendrix.ev3.ai.cluster.DistanceFunc;
import edu.hendrix.ev3.util.Accumulator;
import edu.hendrix.ev3.util.DeepCopyable;
import edu.hendrix.ev3.util.Duple;
import edu.hendrix.ev3.util.FixedSizeArray;
import edu.hendrix.ev3.util.Util;

public class TreeBSOC<T extends Clusterable<T> & DeepCopyable<T>> implements Clusterer<T>, DeepCopyable<TreeBSOC<T>> {
	private BoundedSelfOrgCluster<T> top;
	private FixedSizeArray<FixedSizeArray<BoundedSelfOrgCluster<T>>> tree;
	private DistanceFunc<T> dist;
	private int degree;
	
	public TreeBSOC(int levels, int degree, DistanceFunc<T> dist) {
		Util.assertArgument(levels >= 1, "Must have at least one level");
		tree = FixedSizeArray.make(levels - 1);
		this.dist = dist;
		this.degree = degree;
		
		top = new BoundedSelfOrgCluster<>(degree, dist);
		for (int i = 1; i < levels; i++) {
			FixedSizeArray<BoundedSelfOrgCluster<T>> array = FixedSizeArray.make(Util.pow(degree, i));
			tree.add(array);
			for (int j = 0; j < array.capacity(); j++) {
				array.add(new BoundedSelfOrgCluster<>(degree, dist));
			}
		}
	}
	
	@Override
	public TreeBSOC<T> deepCopy() {
		return new TreeBSOC<>(top.deepCopy(), tree.deepCopy(), dist);
	}

	private TreeBSOC(BoundedSelfOrgCluster<T> top, FixedSizeArray<FixedSizeArray<BoundedSelfOrgCluster<T>>> tree, DistanceFunc<T> dist) {
		this.top = top;
		this.tree = tree;
		this.dist = dist;
	}
	
	@Override
	public int train(T example) {
		Duple<Integer,BoundedSelfOrgCluster<T>> cluster = findLeafCluster(example, (ex, bsoc) -> bsoc.train(ex));
		int where = cluster.getSecond().train(example);
		return where + cluster.getFirst();
	}
	
	@Override
	public Duple<Integer,Long> getClosestNodeDistanceFor(T example) {
		Duple<Integer,BoundedSelfOrgCluster<T>> cluster = findLeafCluster(example, (ex, bsoc) -> bsoc.getClosestMatchFor(ex));
		Duple<Integer,Long> leaf = cluster.getSecond().getClosestNodeDistanceFor(example);
		return new Duple<>(cluster.getFirst() * degree + leaf.getFirst(), leaf.getSecond());
	}
	
	Duple<Integer,BoundedSelfOrgCluster<T>> findLeafCluster(T example, BiFunction<T,BoundedSelfOrgCluster<T>,Integer> bsocSender) {
		BoundedSelfOrgCluster<T> guide = top;
		int offset = 0;
		for (int level = 0; level < tree.size(); level++) {
			offset *= degree;
			offset += bsocSender.apply(example, guide);
			guide = tree.get(level).get(offset);
		}
		return new Duple<>(offset, guide);
	}

	@Override
	public DistanceFunc<T> getDistanceFunc() {
		return dist;
	}

	@Override
	public T getIdealInputFor(int node) {
		int leaf = node / degree;
		int within = node % degree;
		return leaves().get(leaf).getIdealInputFor(within);
	}

	@Override
	public int size() {
		Accumulator total = new Accumulator();
		leaves().doAll((i,bsoc) -> total.add(bsoc.size()));
		return total.getValue();
	}
	
	public int levels() {
		return tree.size() + 1;
	}
	
	public int totalComparisons() {
		return degree * levels();
	}

	@Override
	public ArrayList<Integer> getClusterIds() {
		ArrayList<Integer> ids = new ArrayList<>();
		for (int i = 0; i < leaves().size(); i++) {
			for (int j = 0; j < leaves().get(i).size(); j++) {
				int id = i * degree + j;
				ids.add(id);
			}
		}
		return ids;
	}
	
	private FixedSizeArray<BoundedSelfOrgCluster<T>> leaves() {
		return tree.get(tree.getHighestInUse());
	}
}
