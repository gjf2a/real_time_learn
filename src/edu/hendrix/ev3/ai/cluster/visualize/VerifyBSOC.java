package edu.hendrix.ev3.ai.cluster.visualize;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;

public class VerifyBSOC {
	
	HashMap<String,double[]> targets;

	@Before
	public void setup() {
		targets = new HashMap<>();
		double[] targets2 = new double[3];
		targets2[0] = 8.248110795691657E17;
		targets2[1] = 5.1513807942416422E17;
		targets2[2] = 2.61506540683418176E17;
		targets.put("2", targets2);
		
		double[] targets5 = new double[3];
		targets5[0] = 6.7061296448144691E17;
		targets5[1] = 3.2232476200077939E17;
		targets5[2] = 1.29269961503957824E17;
		targets.put("5", targets5);
	}
	
	void test(String videoName) throws FileNotFoundException {
		System.out.println("Opening video " + videoName);
		ArrayList<AdaptedYUYVImage> inputs = BSOCVideoAssessor.getVideosFrom(videoName);
		int max = 16;
		for (int i = 0; i < targets.get(videoName).length; i++) {
			BSOCVideoAssessor assessor = new BSOCVideoAssessor(inputs, max);
			assertEquals(targets.get(videoName)[i], assessor.ssdInputs2nodes(), 1.0E12);
			max *= 2;
		}
	}

	@Test
	public void testVideo1() throws FileNotFoundException {
		test("2");
	}

	@Test
	public void testVideo2() throws FileNotFoundException {
		test("5");
	}
}
