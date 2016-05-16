package edu.hendrix.ev3.ai.topological.sonar;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map.Entry;

import edu.hendrix.ev3.util.FloatPredicate;

public class Constraint implements FloatPredicate {
	private EnumMap<MinMax,Float> constraints = new EnumMap<>(MinMax.class);
	
	public final static int NUM_BYTES = 2 + 2 * Double.BYTES;
	
	public Constraint() {}
	
	public Constraint(float singleton) {
		this(singleton, singleton);
	}
	
	public Constraint(float min, float max) {
		set(MinMax.MIN, min);
		set(MinMax.MAX, max);
	}
	
	public Constraint(Constraint that) {
		for (Entry<MinMax, Float> entry: that.constraints.entrySet()) {
			set(entry.getKey(), entry.getValue());
		}
	}
	
	public Constraint(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		for (MinMax m: MinMax.values()) {
			byte has = buffer.get();
			float value = buffer.getFloat();
			if (has != 0) set(m, value);
		}
	}
	
	@Override
	public String toString() {
		return "[" + toString(MinMax.MIN) + "," + toString(MinMax.MAX) + "]";
	}
	
	private String toString(MinMax con) {
		return has(con) ? Float.toString(get(con)) : "";
	}
	
	@Override
	public int hashCode() {return toString().hashCode();}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Constraint) {
			Constraint that = (Constraint)other;
			for (Entry<MinMax, Float> m: constraints.entrySet()) {
				if (!that.has(m.getKey()) || that.get(m.getKey()) != m.getValue()) {return false;}
			}
			return constraints.keySet().equals(that.constraints.keySet());
		} else {
			return false;
		}
	}
	
	public byte[] toBytes() {
		ByteBuffer bytes = ByteBuffer.allocate(NUM_BYTES);
		for (MinMax m: MinMax.values()) {
			bytes.put(has(m) ? (byte)-1 : 0);
			bytes.putFloat(has(m) ? get(m) : 0.0f);
		}
		return bytes.array();
	}
	
	public boolean has(MinMax m) {return constraints.containsKey(m);}
	public float get(MinMax m) {return constraints.get(m);}
	public void set(MinMax m, float value) {constraints.put(m, value);}
	
	@Override
	public boolean test(float value) {
		for (Entry<MinMax, Float> m: constraints.entrySet()) {
			if (!m.getKey().meets(m.getValue(), value)) {
				return false;
			}
		}
		return true;
	}
	
	public void tighten(Constraint restriction) {
		for (MinMax m: MinMax.values()) {
			if (restriction.has(m)) {
				set(m, has(m) ? m.other().op(get(m), restriction.get(m)) : restriction.get(m));
			}
		}
	}
	
	public void loosen(Constraint concession) {
		for (MinMax m: MinMax.values()) {
			if (concession.has(m)) {
				set(m, m.op(get(m), concession.get(m)));
			} else {
				constraints.remove(m);
			}
		}
	}
}
