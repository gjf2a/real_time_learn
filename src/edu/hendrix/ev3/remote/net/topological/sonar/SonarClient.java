package edu.hendrix.ev3.remote.net.topological.sonar;

import edu.hendrix.ev3.gui.DelayedQuittingGUI;

public class SonarClient extends DelayedQuittingGUI {
	public static void main(String[] args) {
		setup(250, "SonarClientGUI.fxml");
		launch(args);
	}
}