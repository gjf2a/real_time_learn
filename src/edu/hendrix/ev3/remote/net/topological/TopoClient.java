package edu.hendrix.ev3.remote.net.topological;

import edu.hendrix.ev3.gui.DelayedQuittingGUI;

public class TopoClient extends DelayedQuittingGUI {
	public static void main(String[] args) {
		setup(250, "TopoClientGUI.fxml");
		launch(args);
	}
}