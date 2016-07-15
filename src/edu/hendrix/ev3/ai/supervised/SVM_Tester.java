package edu.hendrix.ev3.ai.supervised;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.TreeMap;
import java.util.Map.Entry;

import edu.hendrix.ev3.ai.TrainingList;
import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.storage.VideoStorage;
import edu.hendrix.ev3.util.Duple;


public class SVM_Tester {
	private static Support_Vector_Machine svm;
	private static double  maxNumTests, maxGamma, maxC, maxCorrect, maxRatio = 0;
	private static ArrayList<Double> posC = new ArrayList<Double>();
	private static ArrayList<Double> posGamma = new ArrayList<Double>();

	public final static int PURGE = 0;
	public static TrainingList retrieveVideos(String id) throws FileNotFoundException {
		VideoStorage videos = VideoStorage.getPCStorage();
		TrainingList result = videos.open(id, EnumSet.allOf(Move.class));
		result.purgeTransitions(PURGE);
		return result;
	}
	private static void storeMax(double num, double numTest, double gamma, double c){
		if (num/numTest > maxRatio){
			maxGamma = gamma;
			maxC = c;
			maxCorrect = num;
			maxNumTests = numTest;
			posC.clear();
			posGamma.clear();
		}
		else if(num/numTest == maxRatio){
			posC.add(c);
			posGamma.add(gamma);
		}
	}
	private static void printMax(){
		System.out.println("FINAL: C: " + maxC);
		System.out.println("Final Gamma: " + maxGamma);
		System.out.println(maxCorrect + "/" + maxNumTests);
		if(posC.size() > 0){
			System.out.println("Possible other values");
			
		}
	}
	public static TreeMap<String,TrainingList> retrieveVideosFrom(String[] strs) {
		TreeMap<String,TrainingList> result = new TreeMap<>();
		for (int i = 0; i < strs.length; i++) {
			try {
				Integer.parseInt(strs[i]);
				System.out.println("Loading video " + strs[i]);
				result.put(strs[i], retrieveVideos(strs[i]));
			} catch (NumberFormatException nfe) {
				// Intentionally left blank
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	public static String getFormattedStats(String title, int num, int denom) {
		int percent = (int)(100 * (float)num / denom);
		return String.format("%s: %d/%d (%d%%)", title, num, denom, percent);
	}

	public static void main(String[] args) throws FileNotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		svm = new Support_Vector_Machine();

		TreeMap<String,TrainingList> ids = retrieveVideosFrom(args);
		for (Entry<String, TrainingList> i: ids.entrySet()) {
			System.out.println("Training svm with " + i.getKey());
			long start = System.currentTimeMillis();
			long duration = System.currentTimeMillis() - start;
			System.out.printf("Duration: %d ms\n", duration);
			for (Entry<String, TrainingList> j: ids.entrySet()) {
				for (int x = -3; x < 15; x++){
					double c = Math.pow(2, x);
					svm.setC(c);
					for (int y = -5; y < 15; y++){
						// DO stuff here
						VideoEvalRobotLearner erl = new VideoEvalRobotLearner(i.getValue(), svm);
						double gamma = Math.pow(2, y);
						svm.setGamma(gamma);
						System.out.printf("Training: %s; Testing: %s\n", i.getKey(), j.getKey());
						erl.test(j.getValue());
						System.out.println("C value: " + Math.pow(2,x));
						System.out.println("Gamma value: " + Math.pow(2, y));
						storeMax((double)erl.numCorrect(),(double)erl.numTests(),c,gamma);
						System.out.println(getFormattedStats("", erl.numCorrect(), erl.numTests()));
						for (Move m: erl.getMovesUsed()) {
							System.out.println(getFormattedStats(m.toString(), erl.numCorrectFor(m), erl.numExamplesFor(m)));
						}
						System.out.println();
					}
				}
				printMax();
				System.out.println();
			}
		}
	}
}

