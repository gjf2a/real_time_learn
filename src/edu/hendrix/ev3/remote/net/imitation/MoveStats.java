package edu.hendrix.ev3.remote.net.imitation;

import edu.hendrix.ev3.remote.Move;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MoveStats {
	private StringProperty moveName;
	private DoubleProperty percent;
	private IntegerProperty correct;
	private IntegerProperty attempts;
	
	public MoveStats(Move move) {
		moveName = new SimpleStringProperty();
		moveName.set(move.toString());
		percent = new SimpleDoubleProperty();
		correct = new SimpleIntegerProperty(0);
		attempts = new SimpleIntegerProperty(0);
	}
	
	public StringProperty moveNameProperty() {return moveName;}
	public DoubleProperty percentProperty() {return percent;}
	public IntegerProperty correctProperty() {return correct;}
	public IntegerProperty attemptsProperty() {return attempts;}
	
	@Override
	public String toString() {
		return String.format("Move %s: %5.2f%% (%d/%d)", moveName.get(), percent.get(), attempts.get(), correct.get());
	}
}
