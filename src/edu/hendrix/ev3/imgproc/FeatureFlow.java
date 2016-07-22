package edu.hendrix.ev3.imgproc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import edu.hendrix.ev3.util.Stdev;
import edu.hendrix.ev3.util.StdevType;
import javafx.scene.paint.Color;

public class FeatureFlow {
	private final static int GREEDY_SEARCH_WINDOW = 6;
	
	private LinkedHashMap<Feature,Feature> old2new;
	private HashMap<Feature,Double> distances;
	private BitImage prev;
	
	private FeatureFlow(LinkedHashMap<Feature,Feature> maker, BiFunction<Feature,Feature,Double> distFunc, BitImage prev) {
		old2new = maker;
		distances = new HashMap<>();
		for (Entry<Feature,Feature> pair: maker.entrySet()) {
			distances.put(pair.getKey(), distFunc.apply(pair.getKey(), pair.getValue()));
		}
		this.prev = new BitImage(prev);
	}
	
	public static <T extends ProcessableImage<T>> FeatureFlow makePatchMatches(Function<T,BitImage> featureFinder, PointPairList matcher, T prev, T next, int windowSize) {
		BitImage prevFeatures = featureFinder.apply(prev);
		BitImage nextFeatures = featureFinder.apply(next);
		LinkedHashMap<Feature,Patch> prevPatches = new LinkedHashMap<>();
		for (Feature pf: prevFeatures.allSet()) {
			prevPatches.put(pf, matcher.makePatch(prev, pf.X(), pf.Y()));
		}
		LinkedHashMap<Feature,Patch> nextPatches = new LinkedHashMap<>();
		for (Feature nf: nextFeatures.allSet()) {
			nextPatches.put(nf, matcher.makePatch(next, nf.X(), nf.Y()));
		}

		LinkedHashMap<Feature,Feature> old2new = new LinkedHashMap<>();
		HashMap<Feature,Integer> distances = new HashMap<>();
		BitImage.visitNeighbors(prevFeatures, nextFeatures, windowSize, (pf, nf) -> {
			int nfDist = prevPatches.get(pf).distance(nextPatches.get(nf));
			if (!distances.containsKey(pf) || nfDist < distances.get(pf)) {
				distances.put(pf, nfDist);
				old2new.put(pf, nf);
			}
		});
		return new FeatureFlow(old2new, (f1, f2) -> (double)distances.get(f1), prevFeatures);
	}
	
	public static <T extends ProcessableImage<T>> FeatureFlow makePatchMatches(Function<T,BitImage> featureFinder, PointPairList matcher, T prev, T next) {
		LinkedHashMap<Feature,Feature> old2new = new LinkedHashMap<>();
		BitImage prevFeatures = featureFinder.apply(prev);
		BitImage nextFeatures = featureFinder.apply(next);
		LinkedHashMap<Feature,Patch> nextPatches = new LinkedHashMap<>();
		for (Feature nf: nextFeatures.allSet()) {
			nextPatches.put(nf, matcher.makePatch(next, nf.X(), nf.Y()));
		}
		HashMap<Feature,Integer> distances = new HashMap<>();
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
				distances.put(pf, bestDist);
			}
		}
		return new FeatureFlow(old2new, (f1, f2) -> (double)distances.get(f1), prevFeatures);
	}
	
	public static <T extends ProcessableImage<T>> FeatureFlow makePatchMatches2(Function<T,BitImage> featureFinder, PointPairList matcher, T prev, T next) {
		LinkedHashMap<Feature,Feature> old2new = new LinkedHashMap<>();
		BitImage prevFeatures = featureFinder.apply(prev);
		BitImage nextFeatures = featureFinder.apply(next);
		LinkedHashMap<Feature,Patch> nextPatches = new LinkedHashMap<>();
		for (Feature nf: nextFeatures.allSet()) {
			nextPatches.put(nf, matcher.makePatch(next, nf.X(), nf.Y()));
		}
		
		HashMap<Feature,Long> distances = new HashMap<>();
		for (Feature pf: prevFeatures.allSet()) {
			Patch p = matcher.makePatch(prev, pf.X(), pf.Y());
			Entry<Feature,Patch> best = null;
			long bestDist = 0;
			for (Entry<Feature, Patch> nf: nextPatches.entrySet()) {
				long nfDist = (p.maxDistance() - p.distance(nf.getValue())) / (long)(1 + Math.sqrt(Feature.euclideanDistanceSquared(pf, nf.getKey())));
				if (best == null || nfDist > bestDist) {
					bestDist = nfDist;
					best = nf;
				}
			}
			if (best != null) {
				old2new.put(pf, best.getKey());
				distances.put(pf, bestDist);
			}
		}
		return new FeatureFlow(old2new, (f1, f2) -> (double)distances.get(f1), prevFeatures);
	}
	
	public boolean hasFeature(Feature key) {
		return old2new.containsKey(key);
	}
	
	public Vector2D toVector(Feature key) {
		return new Vector2D(key, old2new.get(key));
	}
	
	public void filterWith(Function<Map<Feature,Feature>,LinkedHashMap<Feature,Feature>> filter) {
		old2new = filter.apply(old2new);
	}
	
	public void neighborFilterWith(int windowSize, BiPredicate<Vector2D,Vector2D> keepValueMean) {
		ArrayList<Feature> purgees = new ArrayList<>();
		for (Feature f1: old2new.keySet()) {
			ArrayList<Vector2D> inWindow = new ArrayList<>();
			prev.applyToSubimage(f1.X() - windowSize/2, f1.Y() - windowSize/2, windowSize, windowSize, (x, y) -> {
				Feature f2 = new Feature(x, y);
				if (hasFeature(f2)) inWindow.add(toVector(f2));
			});
			
			if (!keepValueMean.test(toVector(f1), Vector2D.mean(inWindow))) {
				purgees.add(f1);
			}
		}
		
		for (Feature purgee: purgees) {
			old2new.remove(purgee);
		}
	}
	
	public void neighborFilterWith(int windowSize, TwoMeanPredicate keep) {
		ArrayList<Feature> purgees = new ArrayList<>();
		for (Feature f1: old2new.keySet()) {
			ArrayList<Vector2D> inWindow = new ArrayList<>();
			prev.applyToSubimage(f1.X() - windowSize/2, f1.Y() - windowSize/2, windowSize, windowSize, (x, y) -> {
				Feature f2 = new Feature(x, y);
				if (hasFeature(f2)) inWindow.add(toVector(f2));
			});
			if (!keep.test(toVector(f1), Vector2D.rMean(inWindow), Vector2D.thetaMean(inWindow))) {
				purgees.add(f1);
			}
		}
		for (Feature purgee: purgees) {
			old2new.remove(purgee);
		}
	}
	
	public static FeatureFlow makeGreedyFAST(BitImage prev, BitImage next) {
		return new FeatureFlow(FAST.getGreedyMatches(prev, next, GREEDY_SEARCH_WINDOW), (f1, f2) -> Math.sqrt(Feature.euclideanDistanceSquared(f1, f2)), prev);
	}
	
	public static FeatureFlow makeStableFAST(BitImage prev, BitImage next) {
		return new FeatureFlow(FAST.getStableMatches(prev, next), (f1, f2) -> Math.sqrt(Feature.euclideanDistanceSquared(f1, f2)), prev);
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
	
	public void keepOnlyMagnitude(double maxStdevs) {
		ArrayList<Feature> keyList = new ArrayList<>(old2new.keySet());
		ArrayList<Vector2D> vecs = vectors();
		ArrayList<Double> mags = new ArrayList<>();
		for (Vector2D vec: vecs) {mags.add(vec.R());}
		Stdev stats = new Stdev(mags, StdevType.POPULATION);
		
		for (int i = 0; i < vecs.size(); i++) {
			if (!stats.within(mags.get(i), maxStdevs)) {
				remove(keyList.get(i));
			}
		}
	}
	
	public void keepOnlyVectors(double maxStdevs) {
		ArrayList<Feature> keyList = new ArrayList<>(old2new.keySet());
		ArrayList<Vector2D> vecs = vectors();
		ArrayList<Double> xs = new ArrayList<>();
		ArrayList<Double> ys = new ArrayList<>();
		for (Vector2D vec: vecs) {
			xs.add(vec.X());
			ys.add(vec.Y());
		}
		Stdev xDev = new Stdev(xs, StdevType.POPULATION);
		Stdev yDev = new Stdev(ys, StdevType.POPULATION);
		
		for (int i = 0; i < vecs.size(); i++) {
			if (!xDev.within(xs.get(i), maxStdevs) || !yDev.within(ys.get(i), maxStdevs)) {
				remove(keyList.get(i));
			}
		}
	}
	
	public void keepOnlyDistance(double maxStdevs) {
		ArrayList<Feature> keyList = new ArrayList<>(old2new.keySet());
		ArrayList<Double> dists = new ArrayList<>();
		for (Feature key: keyList) {dists.add(distances.get(key));}
		Stdev stats = new Stdev(dists, StdevType.POPULATION);
		
		for (int i = 0; i < keyList.size(); i++) {
			if (!stats.within(dists.get(i), maxStdevs)) {
				remove(keyList.get(i));
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
	
	public double getDistanceFor(Feature key) {
		return distances.get(key);
	}
	
	private void remove(Feature key) {
		old2new.remove(key);
		distances.remove(key);
	}
}
