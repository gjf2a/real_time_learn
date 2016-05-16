package edu.hendrix.ev3.remote.net.imitation;

import java.util.EnumSet;
import java.util.Iterator;

import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.util.EnumHistogram;

public class LearningPerformanceStats implements Iterable<Move> {
	private EnumHistogram<Move> attempts;
	private EnumHistogram<Move> correct;
	public static final EnumSet<Move> MOVES_TO_TRACK = EnumSet.of(Move.FORWARD, Move.BACKWARD, Move.LEFT, Move.RIGHT);

	public LearningPerformanceStats() {
		attempts = new EnumHistogram<>(Move.class);
		correct = new EnumHistogram<>(Move.class);
	}
	
	public void record(RobotMessage msg) {
		if (msg.getCurrentMode() == Mode.TRAIN && MOVES_TO_TRACK.contains(msg.getTrainedMove())) {
			attempts.bump(msg.getTrainedMove());
			if (msg.getTrainedMove() == msg.getAppliedMove()) {
				correct.bump(msg.getTrainedMove());
			}
		}
	}
	
	public String getStatsFor(Move m) {
		return String.format("Move %s: %5.2f%% (%d/%d)", m.toString(), (100.0 * correct.getCountFor(m)/attempts.getCountFor(m)), correct.getCountFor(m), attempts.getCountFor(m));
	}

	@Override
	public Iterator<Move> iterator() {
		EnumSet<Move> iterable = EnumSet.copyOf(MOVES_TO_TRACK);
		for (Move m: MOVES_TO_TRACK) {
			if (attempts.getCountFor(m) == 0) {
				iterable.remove(m);
			}
		}
		return iterable.iterator();
	}
}
