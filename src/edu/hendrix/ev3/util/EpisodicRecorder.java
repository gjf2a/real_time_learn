package edu.hendrix.ev3.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;

public class EpisodicRecorder<E extends Enum<E>> {
	private EnumMap<E,ArrayList<Long>> values;
	
	public EpisodicRecorder(Class<E> type) {
		values = new EnumMap<>(type);
	}
	
	protected void addNewEpisode(E label, long value) {
		if (!values.containsKey(label)) {
			values.put(label, new ArrayList<>());
		}
		values.get(label).add(value);
	}
	
	protected void changeEpisodeValue(E label, int episode, long value) {
		values.get(label).set(episode, value);
	}
	
	public boolean hasLabel(E label) {
		return values.containsKey(label);
	}
	
	public Set<E> allLabels() {return Collections.unmodifiableSet(values.keySet());}
	
	public int numEpisodesFor(E label) {
		return hasLabel(label) ? values.get(label).size() : 0;
	}
	
	public int mostRecentEpisodeFor(E label) {
		return numEpisodesFor(label) - 1;
	}
	
	public long getValueFor(E label, int episode) {
		return values.get(label).get(episode);
	}
	
	public boolean hasMostRecentValue(E label) {
		return numEpisodesFor(label) > 0;
	}
	
	public long getRecentValueFor(E label) {
		return getValueFor(label, numEpisodesFor(label) - 1);
	}
	
	public long getTotalValueFor(E label) {
		long total = 0;
		for (int i = 0; i < numEpisodesFor(label); i++) {
			total += getValueFor(label, i);
		}
		return total;
	}
}
