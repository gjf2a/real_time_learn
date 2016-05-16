package edu.hendrix.ev3.remote.net.topological.autosonar;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map.Entry;

import edu.hendrix.ev3.ai.topological.MoveSet;
import edu.hendrix.ev3.remote.Move;

public class MoveApprovalBoxes {
	private EnumMap<Move,CheckBox> boxes = new EnumMap<>(Move.class);
	
	public MoveApprovalBoxes(VBox container, EnumSet<Move> moves) {
		for (Move move: moves) {
			CheckBox box = new CheckBox(move.uiName());
			container.getChildren().add(box);
			boxes.put(move, box);
		}
	}
	
	public MoveSet getSelectedMoves() {
		MoveSet moves = new MoveSet();
		for (Entry<Move, CheckBox> entry: boxes.entrySet()) {
			if (entry.getValue().isSelected()) {
				moves.add(entry.getKey());
			}
		}
		return moves;
	}
}
