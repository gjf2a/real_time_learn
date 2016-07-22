package edu.hendrix.ev3.imgproc;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

import edu.hendrix.ev3.util.DeepCopyable;
import edu.hendrix.ev3.util.StableMatchPrefs;

public class BitImage implements ImageOutline, DeepCopyable<BitImage> {
	private BitSet pixels;
	private int width, height;
	
	public BitImage(BitImage src) {
		this.width = src.width;
		this.height = src.height;
		this.pixels = src.pixels.get(0, width*height);
	}
	
	public BitImage(int width, int height) {
		pixels = new BitSet(width * height);
		pixels.clear();
		this.width = width;
		this.height = height;
	}
	
	public void applyToSubimage(int x, int y, int width, int height, BiConsumer<Integer,Integer> func) {
		int x1 = Math.max(0, x);
		int x2 = Math.min(getWidth() - 1, x + width - 1);
		int y1 = Math.max(0, y);
		int y2 = Math.min(getHeight() - 1, y + height - 1);
		for (int xi = x1; xi <= x2; xi++) {
			for (int yi = y1; yi <= y2; yi++) {
				if (isSet(xi, yi)) {func.accept(xi, yi);}
			}
		}
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
	
	@Override
	public int hashCode() {
		return pixels.hashCode();
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
	
	public static LinkedHashMap<Feature,Feature> getStableMatches(BitImage img1, BitImage img2) {
		return StableMatchPrefs.makeStableMatches(img1.allSet(), img2.allSet(), (m, w) -> (int)(Feature.euclideanDistanceSquared(m, w)));
	}
	
	public static LinkedHashMap<Feature,Feature> getGreedyMatches(BitImage img1, BitImage img2, int searchBound) {
		LinkedHashMap<Feature,Long> bestDistances = new LinkedHashMap<>();
		LinkedHashMap<Feature,Feature> result = new LinkedHashMap<>();
		visitNeighbors(img1, img2, searchBound, (f1, f2) -> {
			long f2distance = Feature.euclideanDistanceSquared(f1, f2);
			if (!bestDistances.containsKey(f1) || f2distance < bestDistances.get(f1)) {
				result.put(f1, f2);
				bestDistances.put(f1, f2distance);
			}
		});
		return result;
	}
	
	public static void visitNeighbors(BitImage img1, BitImage img2, int searchBound, BiConsumer<Feature,Feature> neighborFunc) {
		for (Feature f1: img1.allSet()) {
			img2.applyToSubimage(f1.X() - searchBound/2, f1.Y() - searchBound/2, searchBound, searchBound, (x,y) -> {
				neighborFunc.accept(f1, new Feature(x, y));
			});
		}
	}
}
