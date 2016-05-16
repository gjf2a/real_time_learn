package edu.hendrix.ev3.ai.topological.sonar.visualize;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.BiFunction;

import edu.hendrix.ev3.ai.topological.HierarchicalStateMap;
import edu.hendrix.ev3.ai.topological.StateMap;
import edu.hendrix.ev3.ai.topological.sonar.ClusterableSonarState;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.Duple;

public class CmdLineVisualizer {
	public static void main(String[] args) throws NumberFormatException, FileNotFoundException {
		if (args.length != 2) {
			System.err.println("Usage: Visualizer filename numNodes");
			System.exit(1);
		}
		
		StateMap<ClusterableSonarState> map = makeMapFrom(args[0], Integer.parseInt(args[1]));
		map.assertInvariant();
		System.out.println(args[0] + ", " + args[1]);
		System.out.println("min: " + minFrom(map));
		System.out.println("max: " + maxFrom(map));
		System.out.println(map);
	}
	
	public static ClusterableSonarState from(StateMap<ClusterableSonarState> map, BiFunction<ClusterableSonarState,ClusterableSonarState,ClusterableSonarState> func) {
		ClusterableSonarState result = null;
		for (int node: map.allNodes()) {
			if (result == null) {
				result = map.getIdealInputFor(node);
			} else {
				result = func.apply(result, map.getIdealInputFor(node));
			}
		}
		return result;
	}
	
	public static ClusterableSonarState minFrom(StateMap<ClusterableSonarState> map) {
		return from(map, ClusterableSonarState::min);
	}
	
	public static ClusterableSonarState maxFrom(StateMap<ClusterableSonarState> map) {
		return from(map, ClusterableSonarState::max);
	}

	public static StateMap<ClusterableSonarState> makeMapFrom(String transcriptFile) throws FileNotFoundException {
		File file = new File("logs" + File.separatorChar + transcriptFile);
		Scanner s = new Scanner(file);
		StateMap<ClusterableSonarState> result = null;
		while (s.hasNextLine()) {
			String line = s.nextLine();
			if (line.startsWith("Starting learner: Nodes:")) {
				String[] parts = line.split(" ");
				int numNodes = Integer.parseInt(parts[parts.length - 1]);
				result = ClusterableSonarState.makeStateMap(numNodes);
			} else if (isTrainingLine(line)) {
				String[] parts = line.split(" ");
				String[] currentStateParts = parts[2].split("\\,");
				int currentState = Integer.parseInt(currentStateParts[0]);
				Move currentMove = Move.valueOf(parts[4]);
				if (s.hasNextLine()) {
					ClusterableSonarState state = new ClusterableSonarState(s.nextLine());
					result.addTransition(currentState, currentMove, state);
					result.assertInvariant();
				}		
			}
		}
		s.close();
		return result;
	}
	
	public static StateMap<ClusterableSonarState> makeMapFrom(String transcriptFile, int numNodes) throws FileNotFoundException {
		File file = new File("logs" + File.separatorChar + transcriptFile);
		Scanner s = new Scanner(file);
		StateMap<ClusterableSonarState> result = ClusterableSonarState.makeStateMap(numNodes);
		int currentState = result.getStartingLabel();
		while (s.hasNextLine()) {
			String line = s.nextLine();
			if (isTrainingLine(line)) {
				String[] parts = line.split(" ");
				Move currentMove = Move.valueOf(parts[4]);
				if (s.hasNextLine()) {
					ClusterableSonarState state = new ClusterableSonarState(s.nextLine());
					currentState = result.addTransition(currentState, currentMove, state);
					result.assertInvariant();
				}		
			}
		}
		s.close();
		return result;
	}
	
	public static HierarchicalStateMap<ClusterableSonarState> makeHierarchyFrom(File file, int numNodes) throws FileNotFoundException {
		Scanner s = new Scanner(file);
		HierarchicalStateMap<ClusterableSonarState> result = ClusterableSonarState.makeHierarchicalMap(numNodes);
		int[] currentStates = result.getStartingLabel();
		while (s.hasNextLine()) {
			String line = s.nextLine();
			if (isTrainingLine(line)) {
				String[] parts = line.split(" ");
				Move currentMove = Move.valueOf(parts[4]);
				if (s.hasNextLine()) {
					ClusterableSonarState state = new ClusterableSonarState(s.nextLine());
					currentStates = result.addTransition(currentStates, currentMove, state);
				}
			}
		}
		s.close();
		return result;
	}
	
	public static Duple<ArrayList<ClusterableSonarState>, ArrayList<HierarchicalStateMap<ClusterableSonarState>>> makeHierarchySeqFrom(File file, int numNodes) throws FileNotFoundException {
		ArrayList<ClusterableSonarState> inputs = new ArrayList<>();
		ArrayList<HierarchicalStateMap<ClusterableSonarState>> seq = new ArrayList<>();
		Scanner s = new Scanner(file);
		HierarchicalStateMap<ClusterableSonarState> result = ClusterableSonarState.makeHierarchicalMap(numNodes);
		int[] currentStates = result.getStartingLabel();
		while (s.hasNextLine()) {
			String line = s.nextLine();
			if (isTrainingLine(line)) {
				String[] parts = line.split(" ");
				Move currentMove = Move.valueOf(parts[4]);
				if (s.hasNextLine()) {
					ClusterableSonarState state = new ClusterableSonarState(s.nextLine());
					inputs.add(state);
					currentStates = result.addTransition(currentStates, currentMove, state);
					seq.add(result.deepCopy());
				}
			}
		}
		s.close();
		return new Duple<>(inputs,seq);
	}
	
	public static ArrayList<ClusterableSonarState> getSonarValues(File file) throws FileNotFoundException {
		ArrayList<ClusterableSonarState> inputs = new ArrayList<>();
		Scanner s = new Scanner(file);
		while (s.hasNextLine()) {
			String line = s.nextLine();
			if (isTrainingLine(line)) {
				if (s.hasNextLine()) {
					ClusterableSonarState state = new ClusterableSonarState(s.nextLine());
					inputs.add(state);
				}
			}
		}
		s.close();
		return inputs;
	}
	
	public static boolean isTrainingLine(String line) {
		return line.startsWith("Training");
	}
}
