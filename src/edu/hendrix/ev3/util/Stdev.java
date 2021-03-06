package edu.hendrix.ev3.util;

public class Stdev {
	private double mean, stdev;
	
	public Stdev(Iterable<Double> items, StdevType type) {
		double sum = 0;
		int count = 0;
		for (double item: items) {
			sum += item;
			count += 1;
		}
		mean = sum / count;
		
		sum = 0;
		for (double item: items) {
			sum += Math.pow(mean - item, 2);
		}
		stdev = Math.sqrt(sum / type.varianceDenom(count));
	}
	
	@Override
	public String toString() {
		return String.format("%5.2f +/- %5.2f", mean, stdev);
	}
	
	public double getMean() {return mean;}
	public double getStdev() {return stdev;}
	
	public boolean within(double value, double numStdevs) {
		double lo = mean - stdev * numStdevs;
		double hi = mean + stdev * numStdevs;
		return value >= lo && value <= hi;
	}
}
