package edu.hendrix.ev3.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class Histogram<T> extends SemiAbstractHistogram<T,LinkedHashMap<T,Integer>> {
	public Histogram() {
		super(new LinkedHashMap<T,Integer>());
	}
	
	public Histogram(Histogram<T> other) {
		this();
		Iterator<Entry<T, Integer>> iter = other.iterator();
		while (iter.hasNext()) {
			Entry<T,Integer> entry = iter.next();
			setCountFor(entry.getKey(), entry.getValue());
		}
	}
}
