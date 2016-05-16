package edu.hendrix.ev3.remote.net.topological.autosonar;

import edu.hendrix.ev3.gui.DelayedQuittingGUI;

public class AutoSonar extends DelayedQuittingGUI {
	public static void main(String[] args) {
		setup(250, "AutoSonarGUI.fxml");
		launch(args);
	}
}
