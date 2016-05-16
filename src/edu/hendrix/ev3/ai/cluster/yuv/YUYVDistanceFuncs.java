package edu.hendrix.ev3.ai.cluster.yuv;

public class YUYVDistanceFuncs {
	public static long square(long value) {
		return value * value;
	}
	
	public static long euclideanAllChannels(AdaptedYUYVImage img1, AdaptedYUYVImage img2) {
		long ssd = 0;
		for (int x = 0; x < img1.getWidth(); ++x) {
			for (int y = 0; y < img1.getHeight(); ++y) {
				long yDiff = img1.getY(x, y) - img2.getY(x, y);
				ssd += square(yDiff);
				long uDiff = img1.getU(x, y) - img2.getU(x, y);
				ssd += square(uDiff);
				long vDiff = img1.getV(x, y) - img2.getV(x, y);
				ssd += square(vDiff);
			}
		}
		return ssd;
	}

	public static long euclideanIntensity(AdaptedYUYVImage img1, AdaptedYUYVImage img2) {
		long ssd = 0;
		for (int x = 0; x < img1.getWidth(); ++x) {
			for (int y = 0; y < img1.getHeight(); ++y) {
				long diff = img1.getY(x, y) - img2.getY(x, y);
				ssd += square(diff);
			}
		}
		return ssd;
	}
	
	
}
