package edu.hendrix.ev3.imgproc;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;

import edu.hendrix.ev3.util.DeepCopyable;
import edu.hendrix.ev3.util.StableMatchPrefs;
import edu.hendrix.ev3.util.Util;

public class BitImage implements ImageOutline, DeepCopyable<BitImage> {
	private BitSet pixels;
	private int width, height;
	
	public BitImage(BitImage src) {
		this.pixels = src.pixels.get(0, width*height);
		this.width = src.width;
		this.height = src.height;
	}
	
	public BitImage(int width, int height) {
		pixels = new BitSet(width * height);
		pixels.clear();
		this.width = width;
		this.height = height;
	}
	
	public BitImage getSubimage(int x1, int y1, int x2, int y2) {
		Util.assertArgument(x1 <= x2 && y1 <= y2, "Out-of-order parameters");
		x1 = Math.max(0, x1);
		x2 = Math.min(getWidth() - 1, x2);
		y1 = Math.max(0, y1);
		y2 = Math.min(getHeight() - 1, y2);
		BitImage result = new BitImage(x2 - x1 + 1, y2 - y1 + 1);
		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				if (isSet(x, y)) result.set(x - x1, y - y1);
			}
		}
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				result.append(isSet(x, y) ? "1" : "0");
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	
	public int size() {return pixels.cardinality();}
	
	public void set(int x, int y) {
		pixels.set(index(x, y));
	}
	
	public void clear(int x, int y) {
		pixels.clear(index(x, y));
	}
	
	public void clearAll() {
		pixels.clear();
	}
	
	public boolean isSet(int x, int y) {
		return pixels.get(index(x, y));
	}
	
	int index(int x, int y) {
		return y * width + x;
	}
	
	int xPart(int index) {
		return index % width;
	}
	
	int yPart(int index) {
		return index / width;
	}
	
	public ArrayList<Feature> allSet() {
		ArrayList<Feature> result = new ArrayList<>();
		for (int i = pixels.nextSetBit(0); i >= 0; i = pixels.nextSetBit(i+1)) {
			result.add(new Feature(xPart(i), yPart(i)));
		}
		return result;
	}

	@Override
	public BitImage deepCopy() {
		return new BitImage(this);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof BitImage) {
			BitImage that = (BitImage)other;
			return this.width == that.width && this.height == that.height && this.pixels.equals(that.pixels);
		} else {
			return false;
		}
	}
	
	public static LinkedHashMap<Feature,Feature> getStableMatches(BitImage img1, BitImage img2) {
		return StableMatchPrefs.makeStableMatches(img1.allSet(), img2.allSet(), (m, w) -> (int)(Feature.euclideanDistanceSquared(m, w)));
	}
	
	public static LinkedHashMap<Feature,Feature> getGreedyMatches(BitImage img1, BitImage img2, int searchBound) {
		LinkedHashMap<Feature,Feature> result = new LinkedHashMap<>();
		for (Feature f1: img1.allSet()) {
			Feature best = null;
			long distance = Long.MAX_VALUE;		
			Feature start = new Feature(Math.max(0, f1.X() - searchBound), Math.max(0, f1.Y() - searchBound));
			BitImage sub = img2.getSubimage(start.X(), start.Y(), f1.X() + searchBound, f1.Y() + searchBound);
			for (Feature f2: sub.allSet()) {
				Feature offset = f2.add(start);
				long f2distance = Feature.euclideanDistanceSquared(f1, offset);
				if (best == null || f2distance < distance) {
					best = offset;
					distance = f2distance;
				}
			}
			if (best != null) {
				result.put(f1, best);
			}
		}
		return result;
	}
}
