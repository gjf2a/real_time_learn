package edu.hendrix.ev3.imgproc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

public class RANSAC {
	private static Random rand = new Random();
	
	public static LinkedHashMap<Feature,Feature> 
	filter(Map<Feature,Feature> pairs, 
			int maxIterations,
			int numHypothesisValues,
			double inlierThreshold,
			int minInliers) {
		
		LinkedHashMap<Feature,Feature> result = new LinkedHashMap<>();
		double bestError = Double.MAX_VALUE;
		for (int i = 0; i < maxIterations; i++) {
			LinkedHashMap<Feature,Feature> hypothesis = nRandomPairs(pairs, numHypothesisValues); 
			SimpleMatrix model = makeHomographyFrom(hypothesis);
			LinkedHashMap<Feature,Feature> additionalInliers = new LinkedHashMap<>();
			for (Entry<Feature,Feature> pair: pairs.entrySet()) {
				if (!hypothesis.containsKey(pair.getKey()) && testModelWith(pair.getKey(), pair.getValue(), model) < inlierThreshold) {
					additionalInliers.put(pair.getKey(), pair.getValue());
				}
			}
			if (additionalInliers.size() >= minInliers) {
				additionalInliers.putAll(hypothesis);
				double error = 0;
				for (Entry<Feature,Feature> pair: additionalInliers.entrySet()) {
					error += testModelWith(pair.getKey(), pair.getValue(), model);
				}
				if (error < bestError) {
					bestError = error;
					result = additionalInliers;
				}
			}
		}
		return result;
	}
	
	public static double testModelWith(Feature one, Feature two, SimpleMatrix homography) {
		double[][] v1nums = new double[3][1];
		v1nums[0][0] = one.X();
		v1nums[1][0] = one.Y();
		v1nums[2][0] = 1;
		SimpleMatrix predicted = homography.mult(new SimpleMatrix(v1nums));
		double x2 = predicted.get(0, 0) / predicted.get(2, 0);
		double y2 = predicted.get(1, 0) / predicted.get(2, 0);
		return Math.sqrt(Math.pow(two.X() - x2, 2) + Math.pow(two.Y() - y2, 2));
	}
	
	public static SimpleMatrix makeHomographyFrom(Map<Feature,Feature> pairs) {
		SimpleMatrix A = new SimpleMatrix(buildA(pairs));
		SimpleSVD<SimpleMatrix> svd = new SimpleSVD<>(A.getMatrix(), false);
		SimpleMatrix V = svd.getV();
		double[][] hNums = new double[3][3];
		for (int row = 0; row < 9; row++) {
			hNums[row / 3][row % 3] = V.get(row, 8);
		}
		return new SimpleMatrix(hNums);
	}
	
	public static double[][] buildA(Map<Feature,Feature> pairs) {
		double[][] result = new double[pairs.size() * 2][9];
		int row = 0;
		for (Entry<Feature,Feature> pair: pairs.entrySet()) {
			result[row][0] = -pair.getKey().X();
			result[row][1] = -pair.getKey().Y();
			result[row][2] = -1;
			result[row][3] = 0;
			result[row][4] = 0;
			result[row][5] = 0;
			result[row][6] = pair.getValue().X() * pair.getKey().X();
			result[row][7] = pair.getValue().X() * pair.getKey().Y();
			result[row][8] = pair.getValue().X();
			row += 1;

			result[row][0] = 0;
			result[row][1] = 0;
			result[row][2] = 0;
			result[row][3] = -pair.getKey().X();
			result[row][4] = -pair.getKey().Y();
			result[row][5] = -1;
			result[row][6] = pair.getValue().Y() * pair.getKey().X();
			result[row][7] = pair.getValue().Y() * pair.getKey().Y();
			result[row][8] = pair.getValue().Y();
			row += 1;
		}
		return result;
	}
	
	public static LinkedHashMap<Feature,Feature> nRandomPairs(Map<Feature,Feature> src, int n) {
		ArrayList<Entry<Feature,Feature>> candidates = new ArrayList<>(src.entrySet());
		LinkedHashMap<Feature,Feature> result = new LinkedHashMap<>();
		for (int i = 0; i < n; i++) {
			int pick = rand.nextInt(candidates.size());
			Entry<Feature,Feature> picked = candidates.get(pick);
			result.put(picked.getKey(), picked.getValue());
			candidates.set(pick, candidates.get(candidates.size() - 1));
			candidates.remove(candidates.size() - 1);
		}
		return result;
	}
}
