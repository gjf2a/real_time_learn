package edu.hendrix.ev3.imgproc;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

public class AssessRANSAC {

	@Test
	public void testForward() {
		System.out.println("forward features");
		LinkedHashMap<Feature,Feature> forwardFeatures = new LinkedHashMap<>();
		for (int x = -10; x <= 10; x++) {
			int y1 = (int)Math.round(Math.sqrt(100 - x*x));
			int y2 = -y1;
			int xb = x * 2;
			int yb1 = (int)Math.round(Math.sqrt(400 - xb*xb));
			int yb2 = -yb1;
			forwardFeatures.put(new Feature(x, y1), new Feature(xb, yb1));
			forwardFeatures.put(new Feature(x, y2), new Feature(xb, yb2));
		}
		
		SimpleMatrix homography = RANSAC.makeHomographyFrom(forwardFeatures);
		System.out.println(homography);
		
		for (Entry<Feature, Feature> entry: forwardFeatures.entrySet()) {
			System.out.println(RANSAC.testModelWith(entry.getKey(), entry.getValue(), homography));
		}
	}

	@Test
	public void testLeft() {
		System.out.println("left features");
		LinkedHashMap<Feature,Feature> leftFeatures = new LinkedHashMap<>();
		for (int x = -10; x <= 10; x++) {
			int y1 = (int)Math.round(Math.sqrt(100 - x*x));
			int y2 = -y1;
			int xb = x - 10;
			leftFeatures.put(new Feature(x, y1), new Feature(xb, y1));
			leftFeatures.put(new Feature(x, y2), new Feature(xb, y2));
		}
		
		SimpleMatrix homography = RANSAC.makeHomographyFrom(leftFeatures);
		System.out.println(homography);
		
		for (Entry<Feature, Feature> entry: leftFeatures.entrySet()) {
			System.out.println(RANSAC.testModelWith(entry.getKey(), entry.getValue(), homography));
		}
	}
}
