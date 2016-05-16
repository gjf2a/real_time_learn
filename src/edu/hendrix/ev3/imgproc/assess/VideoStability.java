package edu.hendrix.ev3.imgproc.assess;

import java.io.FileNotFoundException;

import edu.hendrix.ev3.ai.TrainingList;
import edu.hendrix.ev3.storage.VideoStorage;

public class VideoStability {
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length != 2) {
			System.err.println("Usage: java VideoStability videoId Interframe");
			System.exit(1);
		}
		
		VideoStorage videos = VideoStorage.getPCStorage();
		System.out.println("Opening...");
		TrainingList frames = videos.open(args[0]);
		Interframe metric = Interframe.valueOf(args[1]);

		System.out.println("opened; computing total");
		long start = System.currentTimeMillis();
		long total = 0;
		for (int i = 0; i < frames.size() - 1; i++) {
			System.out.print('.');
			if (i % 100 == 99) {
				System.out.println();
			}
			total += metric.computeTotalDistance(frames.get(i).getSecond(), frames.get(i+1).getSecond());
		}
		long duration = System.currentTimeMillis() - start;
		System.out.println();
		double mean = (double)total / (frames.size() - 1);
		System.out.println("Video " + args[0] + " " + metric + ": " + total + " mean: " + mean);
		if (metric == Interframe.EUCLIDEAN) {
			double perPixel = mean / (frames.getHeight() * frames.getWidth());
			System.out.println("Per pixel: " + perPixel);
		} 
		System.out.println("Duration: " + duration);
	}
}
