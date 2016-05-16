package edu.hendrix.ev3.ai.topological.sonar;

import java.util.Map.Entry;
import java.util.function.BiFunction;

import edu.hendrix.ev3.ai.cluster.Clusterable;
import edu.hendrix.ev3.ai.topological.HierarchicalStateMap;
import edu.hendrix.ev3.ai.topological.StateMap;
import edu.hendrix.ev3.remote.sonar.SonarPosition;
import edu.hendrix.ev3.remote.sonar.SonarState;
import edu.hendrix.ev3.remote.sonar.ThreeSonarBot;
import edu.hendrix.ev3.util.DeepCopyable;
import edu.hendrix.ev3.util.Util;

public class ClusterableSonarState extends SonarState implements Clusterable<ClusterableSonarState>, DeepCopyable<ClusterableSonarState> {
	public ClusterableSonarState() {super();}
	public ClusterableSonarState(String src) {super(src);}
	public ClusterableSonarState(ClusterableSonarState other) {super(other);}
	public ClusterableSonarState(ThreeSonarBot bot) {super(bot);}
	public ClusterableSonarState(byte[] bytes) {super(bytes);}
	
	public static StateMap<ClusterableSonarState> makeStateMap(int numNodes) {
		return new StateMap<>(numNodes, ClusterableSonarState::distance);
	}
	
	public static HierarchicalStateMap<ClusterableSonarState> makeHierarchicalMap(int numNodes) {
		return new HierarchicalStateMap<>(numNodes, ClusterableSonarState::distance);
	}

	@Override
	public ClusterableSonarState weightedCentroidWith(ClusterableSonarState other, long thisCount, long otherCount) {
		Util.assertArgument(positionsMatch(other), "Sonar position mismatch");
		ClusterableSonarState result = new ClusterableSonarState();
		for (Entry<SonarPosition, Float> pos: this) {
			float thisPart = pos.getValue() * thisCount;
			float otherPart = other.getReading(pos.getKey()) * otherCount;
			result.setReading(pos.getKey(), (thisPart + otherPart) / (thisCount + otherCount));
		}
		return result;
	}
	
	public static final long DISTANCE_PRECISION = 1000;
	
	public static long distance(SonarState s1, SonarState s2) {
		Util.assertArgument(s1.positionsMatch(s2), "Mismatched sonar states: " + s1 + ", " + s2);
		double sum = 0.0;
		for (Entry<SonarPosition, Float> sonar: s1) {
			sum += Math.pow(sonar.getValue() - s2.getReading(sonar.getKey()), 2);
		}
		return (long)(sum * DISTANCE_PRECISION);
	}
	
	public static ClusterableSonarState combine(ClusterableSonarState s1, ClusterableSonarState s2, BiFunction<Float,Float,Float> op) {
		ClusterableSonarState result = new ClusterableSonarState();
		for (SonarPosition pos: SonarPosition.values()) {
			result.setReading(pos, op.apply(s1.getReading(pos), s2.getReading(pos)));
		}
		return result;
	}
	
	public static ClusterableSonarState min(ClusterableSonarState s1, ClusterableSonarState s2) {
		return combine(s1, s2, (f1, f2) -> Math.min(f1, f2));
	}
	
	public static ClusterableSonarState max(ClusterableSonarState s1, ClusterableSonarState s2) {
		return combine(s1, s2, (f1, f2) -> Math.max(f1, f2));
	}
	
	@Override
	public ClusterableSonarState deepCopy() {
		return new ClusterableSonarState(this);
	}
}
