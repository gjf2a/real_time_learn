package edu.hendrix.ev3.ai.topological.sonar.visualize;

import java.util.EnumMap;
import java.util.Map.Entry;

import edu.hendrix.ev3.ai.topological.sonar.Constraint;
import edu.hendrix.ev3.ai.topological.sonar.MinMax;
import edu.hendrix.ev3.ai.topological.sonar.SonarConstraints;
import edu.hendrix.ev3.remote.sonar.SonarPosition;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class SonarPositionGrid {
	private EnumMap<SonarPosition,EnumMap<MinMax,TextField>> entries = new EnumMap<>(SonarPosition.class);
	
	public static SonarPositionGrid makeFromScratch(GridPane grid) {
		grid.add(new Label("Sonar"), 0, 0);
		SonarPositionGrid result = new SonarPositionGrid();
		for (int col = 0; col < MinMax.values().length; col++) {
			grid.add(new Label(MinMax.values()[col].name()), col + 1, 0);
		}
		for (int row = 0; row < SonarPosition.values().length; row++) {
			grid.add(new Label(SonarPosition.values()[row].name()), 0, row + 1);
			for (int col = 0; col < MinMax.values().length; col++) {
				TextField entry = new TextField();
				grid.add(entry, col + 1, row + 1);
				result.addTextField(SonarPosition.values()[row], entry, MinMax.values()[col]);
			}
		}
		return result;
	}
	
	public void addTextField(SonarPosition pos, TextField tf, MinMax m) {
		if (!entries.containsKey(pos)) {
			entries.put(pos, new EnumMap<>(MinMax.class));
		}
		entries.get(pos).put(m, tf);
	}
	
	public void addMatchingFields(SonarPosition pos, TextField min, TextField max) {
		addTextField(pos, min, MinMax.MIN);
		addTextField(pos, max, MinMax.MAX);
	}
	
	public void clearAll() {
		for (Entry<SonarPosition, EnumMap<MinMax, TextField>> pos: entries.entrySet()) {
			for (Entry<MinMax, TextField> m: pos.getValue().entrySet()) {
				m.getValue().setText("");
			}
		}
	}
	
	public boolean allTextFieldsPresent() {
		for (SonarPosition pos: SonarPosition.values()) {
			if (entries.containsKey(pos)) {
				for (MinMax m: MinMax.values()) {
					if (!entries.get(pos).containsKey(m)) {
						return false;
					}
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	public SonarConstraints getAllConstraints() {
		SonarConstraints result = new SonarConstraints();
		for (SonarPosition pos: entries.keySet()) {
			result.tighten(pos, getConstraintFor(pos));
		}
		return result;
	}
	
	public void setAll(SonarConstraints constraints) {
		for (SonarPosition pos: SonarPosition.values()) {
			for (MinMax m: MinMax.values()) {
				if (constraints.has(pos, m)) {
					entries.get(pos).get(m).setText(Float.toString(constraints.get(pos, m)));
				}
			}
		}
	}
	
	public Constraint getConstraintFor(SonarPosition pos) {
		Constraint c = new Constraint();
		for (Entry<MinMax, TextField> entry: entries.get(pos).entrySet()) {
			try {
				c.set(entry.getKey(), Float.parseFloat(entry.getValue().getText()));
			} catch (NumberFormatException nfe) {
				// Intentionally left blank
			}
		}
		return c;
	}
}
