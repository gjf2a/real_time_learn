package edu.hendrix.ev3.ai.cluster.visualize;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.ai.cluster.yuv.YUYVDistanceFuncs;
import edu.hendrix.ev3.ai.supervised.VideoEvalRobotLearner;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Duple;

public class BSOCVideoAssessor extends BSOCAssessor<AdaptedYUYVImage> {
	public BSOCVideoAssessor(ArrayList<AdaptedYUYVImage> inputs, int numNodes) {
		super(inputs, YUYVDistanceFuncs::euclideanAllChannels, numNodes);
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length < 2) {
			System.out.println("Usage: java BSOCVideoAssessor numNodes storageInt [-shuffle|-all]");
			System.exit(1);
		}
		
		System.out.println("BSOCVideoAssessor");
		System.out.println("numNodes: " + args[0]);
		System.out.println("File:     " + args[1]);
		
		int numNodes = Integer.parseInt(args[0]);
		ArrayList<AdaptedYUYVImage> inputs = getVideosFrom(args[1]);
		if (args.length > 2 && args[2].equals("-shuffle")) {
			Collections.shuffle(inputs);
			System.out.println("Shuffled");
		}
		BSOCVideoAssessor assessor = new BSOCVideoAssessor(inputs, numNodes);
		System.out.println("Input SSD:      " + (double)(assessor.ssdInputs2nodes()));
		System.out.println("Node2Node SSD:  " + (double)(assessor.ssdNodes2Nodes()));
		if (args.length > 2 && args[2].equals("-all")) {
			System.out.println("input,squaredDiff");
			ClusterAssessor.printAll(assessor.squaredDiffs());
		}
	}
	
	public static ArrayList<AdaptedYUYVImage> getVideosFrom(String storageInt) throws FileNotFoundException {
		ArrayList<AdaptedYUYVImage> result = new ArrayList<>();
		for (Duple<Move, AdaptedYUYVImage> img: VideoEvalRobotLearner.retrieveVideos(storageInt)) {
			result.add(img.getSecond());
		}
		return result;
	}
}
