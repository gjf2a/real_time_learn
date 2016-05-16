package edu.hendrix.ev3.gui.actionselector;

import edu.hendrix.ev3.gui.DelayedQuittingGUI;

public class RemoteControl extends DelayedQuittingGUI {
	public static void main(String[] args) {
		setup(300, "RemoteControl.fxml");
		launch(args);
	}
}
