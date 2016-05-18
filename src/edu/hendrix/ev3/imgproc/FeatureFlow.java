package edu.hendrix.ev3.imgproc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.function.Function;

import edu.hendrix.ev3.util.Stdev;
import edu.hendrix.ev3.util.StdevType;
import javafx.scene.paint.Color;

public class FeatureFlow {
	private final static int GREEDY_SEARCH_WINDOW = 6;
	
	private LinkedHashMap<Feature,Feature> old2new;
	
	private FeatureFlow(LinkedHashMap<Feature,Feature> maker) {
		old2new = maker;
	}
	
	public static <T extends ProcessableImage<T>> FeatureFlow makePatchMatches(PointPairList matcher, int numFeatures, ProcessableImage<T> prev, ProcessableImage<T> next) {
		LinkedHashMap<Feature,Feature> old2new = new LinkedHashMap<>();
		FAST prevFeatures = FAST.nFeatures(prev, numFeatures);
		FAST nextFeatures = FAST.nFeatures(next, numFeatures);
		LinkedHashMap<Feature,Patch> nextPatches = new LinkedHashMap<>();
		for (Feature nf: nextFeatures.allSet()) {
			nextPatches.put(nf, matcher.makePatch(next, nf.X(), nf.Y()));
		}
		for (Feature pf: prevFeatures.allSet()) {
			Patch p = matcher.makePatch(prev, pf.X(), pf.Y());
			Feature best = null;
			int bestDist = Integer.MAX_VALUE;
			for (Entry<Feature, Patch> nf: nextPatches.entrySet()) {
				int nfDist = p.distance(nf.getValue());
				if (best == null || nfDist < bestDist) {
					bestDist = nfDist;
					best = nf.getKey();
				}
			}
			if (best != null) {
				old2new.put(pf, best);
			}
		}
		return new FeatureFlow(old2new);
	}
	
	public void filterWith(Function<Map<Feature,Feature>,LinkedHashMap<Feature,Feature>> filter) {
		old2new = filter.apply(old2new);
	}
	
	public static FeatureFlow makeGreedyFAST(BitImage prev, BitImage next) {
		return new FeatureFlow(FAST.getGreedyMatches(prev, next, GREEDY_SEARCH_WINDOW));
	}
	
	public static FeatureFlow makeStableFAST(BitImage prev, BitImage next) {
		return new FeatureFlow(FAST.getStableMatches(prev, next));
	}
	
	public <T extends ProcessableImage<T>> void show(ProcessableImage<T> img) {
		for (Entry<Feature, Feature> vector: old2new.entrySet()) {
			img.setRGB(vector.getKey().X(), vector.getKey().Y(), Color.RED);
			img.setRGB(vector.getValue().X(), vector.getValue().Y(), Color.BLUE);
		}
	}
	
	public ArrayList<Vector2D> vectors() {
		ArrayList<Vector2D> result = new ArrayList<>();
		for (Entry<Feature, Feature> pair: old2new.entrySet()) {
			result.add(new Vector2D(pair.getKey(), pair.getValue()));
		}
		return result;
	}
	
	public void keepOnly(double maxStdevs) {
		ArrayList<Feature> keyList = new ArrayList<>(old2new.keySet());
		ArrayList<Vector2D> vecs = vectors();
		ArrayList<Double> mags = new ArrayList<>();
		for (Vector2D vec: vecs) {mags.add(vec.R());}
		Stdev stats = new Stdev(mags, StdevType.POPULATION);
		
		for (int i = 0; i < vecs.size(); i++) {
			if (!stats.within(mags.get(i), maxStdevs)) {
				old2new.remove(keyList.get(i));
			}
		}
	}
	
	public ArrayList<Vector2D> limitedVectors(double maxStdevs) {
		ArrayList<Vector2D> vecs = vectors();
		ArrayList<Double> mags = new ArrayList<>();
		for (Vector2D vec: vecs) {mags.add(vec.R());}
		Stdev stats = new Stdev(mags, StdevType.POPULATION);
		
		ArrayList<Vector2D> result = new ArrayList<>();
		for (int i = 0; i < vecs.size(); i++) {
			if (stats.within(mags.get(i), maxStdevs)) {
				result.add(vecs.get(i));
			}
		}
		return result;
	}
	
	public Map<Feature,Feature> asMap() {
		return (Map<Feature, Feature>) Collections.unmodifiableMap(old2new);
	}
}
