package edu.hendrix.ev3.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StableMatchPrefs<M,W> {
	private LinkedHashSet<M> freeMen;
	private HashSet<W> freeWomen;
	private HashMap<M,PriorityQueue<W>> prefs;
	private LinkedHashMap<M,W> proposals;
	private HashMap<W,M> acceptances;
	private MatchPredicate<W,M> womanPref;
	
	public StableMatchPrefs(Iterable<M> men, Iterable<W> women, Function<M,Comparator<W>> manPrefMaker, MatchPredicate<W,M> womanPref) {
		freeMen = new LinkedHashSet<>();
		freeWomen = new HashSet<>();
		for (W woman: women) {freeWomen.add(woman);}
		
		prefs = new HashMap<>();
		proposals = new LinkedHashMap<>();
		acceptances = new HashMap<>();
		for (M man: men) {
			freeMen.add(man);
			PriorityQueue<W> options = new PriorityQueue<>(manPrefMaker.apply(man));
			for (W woman: women) {
				options.add(woman);
			}
			prefs.put(man, options);
		}
		
		this.womanPref = womanPref;
	}
	
	public boolean finished() {
		return freeMen.isEmpty() || freeWomen.isEmpty();
	}

	public void matchNext() {
		Util.assertArgument(!finished(), "No further matches possible");
		M man = freeMen.iterator().next();
		freeMen.remove(man);
		for (;;) {
			W option = prefs.get(man).remove();
			M other = acceptances.get(option);
			if (other == null || womanPref.test(option, man, other)) {
				if (other != null) {
					proposals.remove(other);
					freeMen.add(other);
				}
				proposals.put(man, option);
				acceptances.put(option, man);
				freeWomen.remove(option);
				return;
			}
		}
	}
	
	public static <M,W> LinkedHashMap<M,W> makeStableMatches(Iterable<M> men, Iterable<W> women, Function<M,Comparator<W>> manPrefMaker, MatchPredicate<W,M> womanPref) {
		StableMatchPrefs<M,W> prefs = new StableMatchPrefs<>(men, women, manPrefMaker, womanPref);
		while (!prefs.finished()) {
			prefs.matchNext();
		}
		return new LinkedHashMap<>(prefs.proposals);
	}
	
	public static <M,W> LinkedHashMap<M,W> makeStableMatches(Iterable<M> men, Iterable<W> women, BiFunction<M,W,Integer> distance) {
		return makeStableMatches(men, women, m -> (w1, w2) -> distance.apply(m, w1) - distance.apply(m, w2), (w, m1, m2) -> distance.apply(m1, w) < distance.apply(m2, w));
	}
}
