package edu.hendrix.ev3.ai.supervised;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

import edu.hendrix.ev3.ai.TrainingList;
import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.storage.VideoStorage;
import edu.hendrix.ev3.util.Duple;

public class EvalSupervisedClusterer {
	private TrainingList examples, testing;
	private EnumMap<Move,Integer> 
		numExamplesFor = new EnumMap<>(Move.class), 
		numCorrectFor = new EnumMap<>(Move.class),
		numBorderFor = new EnumMap<>(Move.class),
		numCorrectBorderFor = new EnumMap<>(Move.class);
	private ArrayList<TestResult> results;
	private RatioRatings<Move> nodeRatings;
	
	@SuppressWarnings("unchecked")
	public EvalSupervisedClusterer(String trainingID, String testingID, String robotLearnerType) throws FileNotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		this(trainingID, testingID, (SupervisedClusterer<AdaptedYUYVImage,Move>) Class.forName(robotLearnerType).newInstance());
	}
	
	public EvalSupervisedClusterer(String trainingID, String testingID, SupervisedClusterer<AdaptedYUYVImage,Move> learner) throws FileNotFoundException {
		VideoStorage videos = VideoStorage.getPCStorage();
		examples = videos.open(trainingID, EnumSet.allOf(Move.class));
		for (Duple<Move, AdaptedYUYVImage> ex: examples) {
			learner.train(ex.getSecond(), ex.getFirst());
			if (!numCorrectFor.containsKey(ex.getFirst())) {
				numCorrectFor.put(ex.getFirst(), 0);
				numExamplesFor.put(ex.getFirst(), 0);
				numBorderFor.put(ex.getFirst(), 0);
				numCorrectBorderFor.put(ex.getFirst(), 0);
			}
		}
		
		results = new ArrayList<>();
		testing = videos.open(testingID, EnumSet.allOf(Move.class));
		nodeRatings = new RatioRatings<>(Move.class);
		for (Duple<Move, AdaptedYUYVImage> ex: testing) {
			Duple<Move,Integer> learned = learner.bestMatchNodeDistanceFor(ex.getSecond());
			int score = learned.getFirst() == ex.getFirst() ? 1 : 0;
			numCorrectFor.put(ex.getFirst(), score+numCorrectFor.get(ex.getFirst()));
			numExamplesFor.put(ex.getFirst(), 1+numExamplesFor.get(ex.getFirst()));
			if (learner.isBorderNode(learned.getFirst(), learned.getSecond())) {
				numBorderFor.put(ex.getFirst(), 1+numBorderFor.get(ex.getFirst()));
				numCorrectBorderFor.put(ex.getFirst(), score+numCorrectBorderFor.get(ex.getFirst()));
			}
			nodeRatings.rateNodeWith(learned, ex.getFirst());
			results.add(new TestResult(ex.getFirst(), learned.getFirst(), learner.bestRankedAlternative(ex.getSecond())));
		}

		EnumMap<Move,TreeSet<Integer>> borderNodes = learner.getBorderNodes();
		String clusterInfo = "Clusters for: ";
		for (Move m: getMovesUsed()) {
			clusterInfo += m.toString() + String.format("(%d)[Border:%d] ", learner.numClustersWith(m), borderNodes.get(m).size());
		}
		System.out.println(clusterInfo);
	}
	
	public int numCorrect() {
		int total = 0;
		for (Move m: numCorrectFor.keySet()) {
			total += numCorrectFor(m);
		}
		return total;
	}
	public int numTests() {return testing.size();}
	
	public int numCorrectFor(Move m) {return numCorrectFor.get(m);}
	public int numExamplesFor(Move m) {return numExamplesFor.get(m);}
	
	public int numBorderFor(Move m) {return numBorderFor.get(m);}
	public int numCorrectBorderFor(Move m) {return numCorrectBorderFor.get(m);}
	
	public Set<Move> getMovesUsed() {return numExamplesFor.keySet();}
	
	public Duple<Integer,Integer> getRatioBestAlternativeFor(Move test, Move result) {
		int total = 0;
		int num = 0;
		for (TestResult info: results) {
			if (info.matches(test, result)) {
				total += info.getBestAlternative();
				num += 1;
			}
		}
		return new Duple<>(total, num);
	}
	
	public static String getFormattedStats(String title, int num, int denom) {
		int percent = (int)(100 * (float)num / denom);
		return String.format("%s: %d/%d (%d%%)", title, num, denom, percent);
	}
	
	public static ArrayList<String> retrieveIntsFrom(String[] strs) {
		ArrayList<String> result = new ArrayList<>();
		for (int i = 0; i < strs.length; i++) {
			try {
				Integer.parseInt(strs[i]);
				result.add(strs[i]);
			} catch (NumberFormatException nfe) {
				// Intentionally left blank
			}
		}
		return result;
	}
	
	public static void main(String[] args) throws FileNotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (args.length < 2) {
			System.out.println("Usage: EvalSupervisedClusterer ids... RobotLearnerType...");
			System.exit(1);
		}
		
		ArrayList<String> ids = retrieveIntsFrom(args);

		for (String i: ids) {
			for (String j: ids) {
				System.out.printf("Training: %s; Testing: %s\n", i, j);
				for (int k = ids.size(); k < args.length; k++) {
					long start = System.currentTimeMillis();
					EvalSupervisedClusterer erl = new EvalSupervisedClusterer(i, j, args[k]);
					long duration = System.currentTimeMillis() - start;
					System.out.println(getFormattedStats(args[k], erl.numCorrect(), erl.numTests()));
					System.out.printf("Duration: %d ms\n", duration);
					for (Move m: erl.getMovesUsed()) {
						System.out.println(getFormattedStats(m.toString(), erl.numCorrectFor(m), erl.numExamplesFor(m)));
						System.out.println(getFormattedStats("Border nodes: ", erl.numCorrectBorderFor(m), erl.numBorderFor(m)));
					}
					for (Move test: Move.values()) {
						for (Move reply: Move.values()) {
							Duple<Integer,Integer> ratio = erl.getRatioBestAlternativeFor(test, reply);
							if (ratio.getSecond() > 0) {
								double mean = (double)ratio.getFirst() / ratio.getSecond();
								System.out.printf("%s -> %s: %4.2f (%d/%d)\n", test.toString(), reply.toString(), mean, ratio.getFirst(), ratio.getSecond());
							}
						}
					}
				}
				System.out.println();
			}
		}
	}
}
