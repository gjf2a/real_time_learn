package edu.hendrix.ev3.imgproc;

public class AngleHistogram {
	private int[] counts;
	private int total;
	
	public AngleHistogram(int numBuckets) {
		counts = new int[numBuckets];
		total = 0;
	}
	
	static double fixAngle(double angle) {
		while (angle >= Math.PI*2) {
			angle -= Math.PI*2;
		}
		while (angle < 0) {
			angle += Math.PI*2;
		}
		return angle;
	}
	
	public void bump(double angle) {
		int bucket = (int)(fixAngle(angle) * counts.length / (2.0 * Math.PI));
		counts[bucket]++;
		total++;
	}
	
	public int size() {return counts.length;}
	
	public int getCountFor(int bucket) {return counts[bucket];}
	
	public double getPortionFor(int bucket) {return counts[bucket] / (double)total;}
	
	public double getBucketSize() {return Math.PI * 2 / counts.length;}
}
