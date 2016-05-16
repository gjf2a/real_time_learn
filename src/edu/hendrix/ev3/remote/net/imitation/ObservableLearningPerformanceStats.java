package edu.hendrix.ev3.remote.net.imitation;


import java.util.EnumMap;

import edu.hendrix.ev3.remote.Move;
import edu.hendrix.ev3.remote.net.UpdateListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ObservableLearningPerformanceStats implements UpdateListener<RobotMessage> {
	private EnumMap<Move,MoveStats> stats;
	private ObservableList<MoveStats> statList;
	
	public ObservableLearningPerformanceStats() {
		stats = new EnumMap<>(Move.class);
		statList = FXCollections.observableArrayList();
		for (Move move: LearningPerformanceStats.MOVES_TO_TRACK) {
			MoveStats statRow = new MoveStats(move);
			statList.add(statRow);
			stats.put(move, statRow);
		}
	}
	
	public ObservableList<MoveStats> getObservableList() {return statList;}
	
	public void report(RobotMessage msg) {
		if (msg.getCurrentMode() == Mode.TRAIN) {
			MoveStats stat = stats.get(msg.getTrainedMove());
			int updatedAttempts = stat.attemptsProperty().get() + 1;
			stat.attemptsProperty().set(updatedAttempts);
			int correct = stat.correctProperty().get();
			if (msg.getAppliedMove() == msg.getTrainedMove()) {correct += 1;}
			stat.correctProperty().set(correct);
			double percent = 100.0 * correct / updatedAttempts;
			stat.percentProperty().set(percent);
		}
	}
}
