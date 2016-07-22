package edu.hendrix.ev3.imgproc;

import java.util.HashMap;
import java.util.Map;

import edu.hendrix.ev3.util.Util;
import edu.hendrix.ev3.util.ValueWrap;

public class BitImageClusters {
	private BitImage img;
	private Map<Feature,Integer> counts;
	
	public BitImageClusters(BitImage src) {
		img = new BitImage(src);
		counts = new HashMap<>();
		for (Feature f: src.allSet()) {
			counts.put(f, 1);
		}
	}
	
	public BitImage getBitImage() {
		return new BitImage(img);
	}
	
	public int size() {return img.size();}
	
	public Feature combinationOf(Feature f1, Feature f2) {
		return f1.weightedCentroidWith(f2, counts.get(f1), counts.get(f2));
	}
	
	public Feature combine(Feature f1, Feature f2) {
		if (f1.equals(f2)) {
			return f1;
		} else {
			Feature combo = combinationOf(f1, f2);
			img.clear(f1.X(), f1.Y());
			img.clear(f2.X(), f2.Y());
			int f1Count = counts.remove(f1);
			int f2Count = counts.remove(f2);
			if (counts.containsKey(combo)) {
				Util.assertState(img.isSet(combo.X(), combo.Y()), "What?");
				counts.put(combo, counts.get(combo) + f1Count + f2Count);
			} else {
				img.set(combo.X(), combo.Y());
				counts.put(combo, f1Count + f2Count);
			}
			return combo;
		}
	}
	
	public void combineAllIn(int x, int y, int width, int height) {
		ValueWrap<Feature> last = new ValueWrap<>(null);
		img.applyToSubimage(x, y, width, height, (xi, yi) -> {
			if (img.isSet(xi, yi)) {
					Feature current = new Feature(xi, yi);
					if (last.value == null) {
						last.value = current;
					} else {
						last.value = combine(last.value, current);
					}
				}
			});
	}
	
	public void combineAllZones(int zoneSize) {
		for (int x = 0; x < img.getWidth(); x += zoneSize) {
			for (int y = 0; y < img.getHeight(); y += zoneSize) {
				combineAllIn(x, y, zoneSize, zoneSize);
			}
		}
	}
}
