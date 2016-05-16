package edu.hendrix.ev3.ai.topological.sonar;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.function.Predicate;

import edu.hendrix.ev3.ai.topological.Path;
import edu.hendrix.ev3.ai.topological.PathsFrom;
import edu.hendrix.ev3.ai.topological.StateMap;
import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.sonar.SonarPosition;
import edu.hendrix.ev3.util.Util;

public class SonarConstraints implements Predicate<ClusterableSonarState> {
	private EnumMap<SonarPosition,Constraint> constraints = new EnumMap<>(SonarPosition.class);
	
	public final static int NUM_BYTES = SonarPosition.values().length * (1 + Constraint.NUM_BYTES);
	
	public SonarConstraints() {
		for (SonarPosition pos: SonarPosition.values()) {
			constraints.put(pos, new Constraint());
		}
	}
	
	public SonarConstraints(ClusterableSonarState startingPoint) {
		for (SonarPosition pos: SonarPosition.values()) {
			constraints.put(pos, new Constraint(startingPoint.getReading(pos)));
		}
	}
	
	public void strengthen(SonarConstraints restriction) {
		for (SonarPosition pos: SonarPosition.values()) {
			tighten(pos, restriction.constraints.get(pos));
		}
	}
	
	public boolean has(SonarPosition pos, MinMax m) {
		return constraints.containsKey(pos) && constraints.get(pos).has(m);
	}
	
	public float get(SonarPosition pos, MinMax m) {
		return constraints.get(pos).get(m);
	}
	
	public void tighten(SonarPosition pos, Constraint restriction) {
		if (constraints.containsKey(pos)) {
			constraints.get(pos).tighten(restriction);
		} else {
			constraints.put(pos, restriction);
		}
	}
	
	public void loosen(SonarConstraints concession) {
		for (SonarPosition pos: SonarPosition.values()) {
			loosen(pos, concession.constraints.get(pos));
		}
	}
	
	public void loosen(SonarPosition pos, Constraint concession) {
		if (constraints.containsKey(pos)) {
			constraints.get(pos).loosen(concession);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Entry<SonarPosition, Constraint> con: constraints.entrySet()) {
			result.append(con.getKey());
			result.append('{');
			result.append(con.getValue());
			result.append('}');
		}
		return result.toString();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof SonarConstraints) {
			SonarConstraints that = (SonarConstraints)other;
			return this.constraints.equals(that.constraints);
		} else {
			return false;
		}
	}
	
	public static SonarConstraints fromBytes(byte[] bytes) {
		SonarConstraints result = new SonarConstraints();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		byte[] conBytes = new byte[Constraint.NUM_BYTES];
		while (buffer.hasRemaining()) {
			SonarPosition pos = SonarPosition.values()[buffer.get()];
			buffer.get(conBytes);
			result.constraints.put(pos, new Constraint(conBytes));
		}
		return result;
	}
	
	public byte[] toBytes() {
		ByteBuffer bytes = ByteBuffer.allocate(NUM_BYTES);
		for (SonarPosition pos: SonarPosition.values()) {
			bytes.put((byte)pos.ordinal());
			bytes.put((constraints.containsKey(pos) ? constraints.get(pos) : new Constraint()).toBytes());
		}
		return bytes.array();
	}
	
	public Move bestMoveFor(StateMap<ClusterableSonarState> map, ClusterableSonarState current) {
		ArrayList<Integer> candidates = map.acceptableNodes(s -> test(s));
		if (candidates.size() > 0) {
			PathsFrom paths = map.bestPathsFrom(current);
			if (paths.getNumDestinations() > 0) {
				Path best = paths.bestPathTo(candidates);
				return best.getFirstMove();
			} else {
				return Move.NONE;
			}
		} else {
			return Move.NONE;
		}
	}
	
	@Override
	public boolean test(ClusterableSonarState state) {
		Util.assertArgument(state.positionsMatch(constraints.keySet()), "Position mismatch");
		for (Entry<SonarPosition, Float> sonar: state) {
			if (constraints.containsKey(sonar.getKey()) && !constraints.get(sonar.getKey()).test(sonar.getValue())) {
				return false;
			}
		}
		return true;
	}
}
