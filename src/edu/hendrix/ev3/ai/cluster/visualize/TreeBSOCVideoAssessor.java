package edu.hendrix.ev3.ai.cluster.visualize;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.cluster.yuv.YUYVDistanceFuncs;

public class TreeBSOCVideoAssessor extends TreeBSOCAssessor<AdaptedYUYVImage> {
	public TreeBSOCVideoAssessor(ArrayList<AdaptedYUYVImage> inputs, int levels, int degree) {
		super(inputs, YUYVDistanceFuncs::euclideanAllChannels, levels, degree);
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length < 2) {
			System.out.println("Usage: java TreeBSOCVideoAssessor levels degree storageInt [-shuffle|-all]");
			System.exit(1);
		}
		
		int levels = Integer.parseInt(args[0]);
		int degree = Integer.parseInt(args[1]);

		System.out.println("TreeBSOCVideoAssessor");
		System.out.println("levels:      " + levels);
		System.out.println("degree:      " + degree);
		System.out.println("comparisons: " + (levels * degree));
		System.out.println("File:        " + args[2]);
		
		ArrayList<AdaptedYUYVImage> inputs = BSOCVideoAssessor.getVideosFrom(args[2]);
		if (args.length > 3 && args[3].equals("-shuffle")) {
			Collections.shuffle(inputs);
			System.out.println("Shuffled");
		}
		TreeBSOCVideoAssessor assessor = new TreeBSOCVideoAssessor(inputs, levels, degree);
		System.out.println("Input SSD:      " + (double)(assessor.ssdInputs2nodes()));
		System.out.println("Node2Node SSD:  " + (double)(assessor.ssdNodes2Nodes()));
		if (args.length > 3 && args[3].equals("-all")) {
			System.out.println("input,squaredDiff");
			ClusterAssessor.printAll(assessor.squaredDiffs());
		}
	}
}
