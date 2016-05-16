package edu.hendrix.ev3.util;

import java.util.EnumMap;

public class Stopwatches<E extends Enum<E>> extends EpisodicRecorder<E> {
	private EnumMap<E,Stopwatch> watches;
	
	public Stopwatches(Class<E> enumType) {
		super(enumType);
		watches = new EnumMap<>(enumType);
	}

	public boolean episodeInProgress(E label) {
		return watches.containsKey(label) && watches.get(label).isRunning();
	}
	
	public void startEpisodeFor(E label) {
		if (!hasLabel(label)) {
			watches.put(label, new Stopwatch());
		}
		if (episodeInProgress(label)) {
			endEpisodeFor(label);
		}
		addNewEpisode(label, 0);
		watches.get(label).start();
	}

	public void endEpisodeFor(E label) {
		if (episodeInProgress(label)) {
			watches.get(label).stop();
			changeEpisodeValue(label, mostRecentEpisodeFor(label), watches.get(label).getDurationMillis());
		}
	}
	
	public void stopAll() {
		for (E label: allLabels()) {
			endEpisodeFor(label);
		}
	}
}
