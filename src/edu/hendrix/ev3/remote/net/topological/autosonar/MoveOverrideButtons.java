package edu.hendrix.ev3.remote.net.topological.autosonar;

import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map.Entry;

import edu.hendrix.ev3.remote.Move;

public class MoveOverrideButtons {
	private EnumMap<Move,RadioButton> buttons = new EnumMap<>(Move.class);
	private ToggleGroup group = new ToggleGroup();
	
	public MoveOverrideButtons(VBox container) {
		for (Move move: Move.values()) {
			RadioButton box = new RadioButton(move.uiName());
			group.getToggles().add(box);
			container.getChildren().add(box);
			buttons.put(move, box);
			box.setSelected(true);
		}
	}
	
	public Move getSelectedMove() {
		for (Entry<Move, RadioButton> entry: buttons.entrySet()) {
			if (entry.getValue().isSelected()) {
				return entry.getKey();
			}
		}
		return Move.NONE;
	}
}
