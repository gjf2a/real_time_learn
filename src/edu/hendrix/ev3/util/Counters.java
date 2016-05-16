package edu.hendrix.ev3.util;


public class Counters<E extends Enum<E>> extends EpisodicRecorder<E> {
	public Counters(Class<E> type) {
		super(type);
	}
	
	public void bump(E counter) {
		if (!hasLabel(counter)) {
			restart(counter);
		}
		changeEpisodeValue(counter, mostRecentEpisodeFor(counter), getRecentValueFor(counter) + 1);
	}
	
	public void restart(E counter) {
		addNewEpisode(counter, 0);
	}
}
