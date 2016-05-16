package edu.hendrix.ev3.imgproc.assess;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.cluster.yuv.YUYVDistanceFuncs;
import edu.hendrix.ev3.imgproc.FAST;

public enum Interframe {
	
	EUCLIDEAN {
		@Override
		public long computeTotalDistance(AdaptedYUYVImage img1, AdaptedYUYVImage img2) {
			return YUYVDistanceFuncs.euclideanIntensity(img1, img2);
		}
	}, FAST_FILTER {
		@Override
		public long computeTotalDistance(AdaptedYUYVImage img1, AdaptedYUYVImage img2) {
			FAST corners1 = FAST.nFeatures(img1, NUM_FEATURES);
			FAST corners2 = FAST.nFeatures(img2, NUM_FEATURES);
			return FAST.totalDistance(FAST.getStableMatches(corners1, corners2));
		}
	}, FAST_GREEDY {
		@Override
		public long computeTotalDistance(AdaptedYUYVImage img1, AdaptedYUYVImage img2) {
			FAST corners1 = FAST.nFeatures(img1, NUM_FEATURES);
			FAST corners2 = FAST.nFeatures(img2, NUM_FEATURES);
			return FAST.totalDistance(FAST.getGreedyMatches(corners1, corners2, 6));
		}
	}, FAST_CLUSTER_1 {
		@Override
		public long computeTotalDistance(AdaptedYUYVImage img1, AdaptedYUYVImage img2) {
			FAST corners1 = FAST.nClusters(img1, NUM_FEATURES);
			FAST corners2 = FAST.nClusters(img2, NUM_FEATURES);
			return FAST.totalDistance(FAST.getStableMatches(corners1, corners2));
		}
	};
	
	abstract public long computeTotalDistance(AdaptedYUYVImage img1, AdaptedYUYVImage img2);
	public static final int NUM_FEATURES = 500;
}
