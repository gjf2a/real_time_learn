package edu.hendrix.ev3.util;

public interface MatchPredicate<A,B> {
	public boolean test(A a, B b1, B b2);
}
