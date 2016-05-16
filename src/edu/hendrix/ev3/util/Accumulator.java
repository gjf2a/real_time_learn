package edu.hendrix.ev3.util;

/**
 * 
 * @author gabriel
 *
 * This is a simple int wrapper.
 * It exists in order to be used in closures, which do not permit an unwrapped int or 
 * even an "Integer".
 */
public class Accumulator {
	private int value;
	
	public Accumulator() {this(0);}
	
	public Accumulator(int start) {
		value = start;
	}
	
	public void add(int up) {value += up;}
	
	public void mod(int dividend) {value %= dividend;}
	
	public int getValue() {return value;}

	@Override
	public String toString() {return Integer.toString(value);}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Accumulator) {
			Accumulator that = (Accumulator)other;
			return this.value == that.value;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {return value;}
}
